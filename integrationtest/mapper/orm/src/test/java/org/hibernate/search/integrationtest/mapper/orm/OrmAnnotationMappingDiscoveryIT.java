/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.mapper.orm;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.search.mapper.orm.cfg.SearchOrmSettings;
import org.hibernate.search.mapper.orm.mapping.HibernateOrmMappingDefinition;
import org.hibernate.search.mapper.orm.mapping.HibernateOrmSearchMappingContributor;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Field;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.integrationtest.mapper.orm.bridge.CustomMarkerConsumingPropertyBridge;
import org.hibernate.search.integrationtest.mapper.orm.bridge.annotation.CustomMarkerAnnotation;
import org.hibernate.search.integrationtest.mapper.orm.bridge.annotation.CustomMarkerConsumingPropertyBridgeAnnotation;
import org.hibernate.search.util.impl.integrationtest.common.rule.BackendMock;
import org.hibernate.search.util.impl.integrationtest.common.stub.backend.index.impl.StubBackendFactory;
import org.hibernate.search.util.impl.test.rule.StaticCounters;
import org.hibernate.service.ServiceRegistry;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

public class OrmAnnotationMappingDiscoveryIT {

	private static final String PREFIX = SearchOrmSettings.PREFIX;

	@Rule
	public BackendMock backendMock = new BackendMock( "stubBackend" );

	@Rule
	public StaticCounters counters = new StaticCounters();

	private SessionFactory sessionFactory;

	@Test
	public void discoveryEnabled() {
		StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder()
				.applySetting( PREFIX + "backend.stubBackend.type", StubBackendFactory.class.getName() )
				.applySetting( PREFIX + "index.default.backend", "stubBackend" )
				.applySetting( SearchOrmSettings.MAPPING_CONTRIBUTOR, new HibernateOrmSearchMappingContributor() {
					@Override
					public void contribute(HibernateOrmMappingDefinition definition) {
						definition.programmaticMapping()
								.type( IndexedEntity.class )
										.property( "nonAnnotationMappedEmbedded" )
												.indexedEmbedded();
					}
				} );

		ServiceRegistry serviceRegistry = registryBuilder.build();

		// We register NonExplicitlyRegistered* types here, but it's only for Hibernate ORM.
		// Only entity types will be passed to Hibernate Search.
		MetadataSources ms = new MetadataSources( serviceRegistry )
				.addAnnotatedClass( IndexedEntity.class )
				.addAnnotatedClass( NonExplicitlyRegisteredType.class )
				.addAnnotatedClass( NonExplicitlyRegisteredNonMappedType.class )
				.addAnnotatedClass( NonExplicitlyRegisteredNonAnnotationMappedType.class );

		Metadata metadata = ms.buildMetadata();

		final SessionFactoryBuilder sfb = metadata.getSessionFactoryBuilder();

		backendMock.expectSchema( IndexedEntity.INDEX, b -> b
				.objectField( "annotationMappedEmbedded", b2 -> b2
						/*
						 * This field will only be added if the bridge is applied, which means:
						 * a) that the annotation mapping for the embedded type has been automatically discovered
						 * b) that the annotation mapping for the type on which the bridge is applied
						 * has been automatically discovered
						 */
						.objectField( "annotatedProperty", b3 -> {
							// We do not expect any particular property in the object field added by the bridge
						} )
				)
				.objectField( "nonAnnotationMappedEmbedded", b2 -> b2
						/*
						 * This field will be discovered automatically even though it is declared in an annotated type
						 * which has not been registered explicitly.
						 */
						.field( "text", String.class )
				)
		);

		sessionFactory = sfb.build();
		backendMock.verifyExpectationsMet();
	}

	@Test
	public void discoveryDisabled() {
		StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder()
				.applySetting( PREFIX + "backend.stubBackend.type", StubBackendFactory.class.getName() )
				.applySetting( PREFIX + "index.default.backend", "stubBackend" )
				.applySetting( SearchOrmSettings.ENABLE_ANNOTATION_MAPPING, "false" )
				.applySetting( SearchOrmSettings.MAPPING_CONTRIBUTOR, new HibernateOrmSearchMappingContributor() {
					@Override
					public void contribute(HibernateOrmMappingDefinition definition) {
						definition.programmaticMapping()
								.type( IndexedEntity.class )
										.property( "nonAnnotationMappedEmbedded" )
												.indexedEmbedded();

						/*
						 * Annotations should be completely ignored.
						 * We add some of the annotation mapping programmatically,
						 * just to check that discovery is disabled for nested types.
						 */
						definition.programmaticMapping()
								.type( IndexedEntity.class ).indexed( IndexedEntity.INDEX )
										.property( "id" ).documentId()
										.property( "annotationMappedEmbedded" )
												.indexedEmbedded();
					}
				} );

		ServiceRegistry serviceRegistry = registryBuilder.build();

		// We register NonExplicitlyRegistered* types here, but it's only for Hibernate ORM.
		// None of those types will be passed to Hibernate Search, since annotation mapping is disabled.
		MetadataSources ms = new MetadataSources( serviceRegistry )
				.addAnnotatedClass( IndexedEntity.class )
				.addAnnotatedClass( NonExplicitlyRegisteredType.class )
				.addAnnotatedClass( NonExplicitlyRegisteredNonMappedType.class )
				.addAnnotatedClass( NonExplicitlyRegisteredNonAnnotationMappedType.class );

		Metadata metadata = ms.buildMetadata();

		final SessionFactoryBuilder sfb = metadata.getSessionFactoryBuilder();

		backendMock.expectSchema( IndexedEntity.INDEX, b -> b
				.objectField( "annotationMappedEmbedded", b2 -> {
					/*
					 * This object field should be empty because
					 * the annotation mapping for the embedded type has *NOT* been automatically discovered.
					 */
				} )
				.objectField( "nonAnnotationMappedEmbedded", b2 -> {
					/*
					 * This object field should be empty because
					 * the annotation mapping for the embedded type has *NOT* been automatically discovered.
					 */
				} )
		);

		sessionFactory = sfb.build();
		backendMock.verifyExpectationsMet();
	}

	@After
	public void cleanup() {
		if ( sessionFactory != null ) {
			sessionFactory.close();
		}
	}

	@Entity(name = "indexed")
	@Indexed(index = IndexedEntity.INDEX)
	public static final class IndexedEntity {
		public static final String INDEX = "IndexedEntity";

		@Id
		private Integer id;

		@Embedded
		@IndexedEmbedded
		private NonExplicitlyRegisteredType annotationMappedEmbedded;

		@Embedded
		private NonExplicitlyRegisteredNonAnnotationMappedType nonAnnotationMappedEmbedded;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public NonExplicitlyRegisteredType getAnnotationMappedEmbedded() {
			return annotationMappedEmbedded;
		}

		public void setAnnotationMappedEmbedded(NonExplicitlyRegisteredType annotationMappedEmbedded) {
			this.annotationMappedEmbedded = annotationMappedEmbedded;
		}

		public NonExplicitlyRegisteredNonAnnotationMappedType getNonAnnotationMappedEmbedded() {
			return nonAnnotationMappedEmbedded;
		}

		public void setNonAnnotationMappedEmbedded(
				NonExplicitlyRegisteredNonAnnotationMappedType nonAnnotationMappedEmbedded) {
			this.nonAnnotationMappedEmbedded = nonAnnotationMappedEmbedded;
		}
	}

	/**
	 * A type that is not registered explicitly, but mentioned in an indexed-embedded property.
	 * It should be automatically discovered when applying the indexed-embedded,
	 * BUT the fact that it is indexed should be ignored (only explicitly registered types are indexed).
	 */
	@Embeddable
	@Indexed(index = "SHOULD_NOT_BE_INDEXED")
	public static class NonExplicitlyRegisteredType {
		@CustomMarkerConsumingPropertyBridgeAnnotation
		private NonExplicitlyRegisteredNonMappedType content;

		public NonExplicitlyRegisteredNonMappedType getContent() {
			return content;
		}

		public void setContent(NonExplicitlyRegisteredNonMappedType content) {
			this.content = content;
		}
	}

	/**
	 * A type that is neither registered explicitly, nor mentioned in any mapped property,
	 * but should be automatically discovered when the {@link CustomMarkerConsumingPropertyBridge} inspects the metamodel;
	 * if it isn't, the bridge will not contribute any field.
	 */
	@Embeddable
	public static class NonExplicitlyRegisteredNonMappedType {
		@CustomMarkerAnnotation
		private Integer annotatedProperty;

		public Integer getAnnotatedProperty() {
			return annotatedProperty;
		}

		public void setAnnotatedProperty(Integer annotatedProperty) {
			this.annotatedProperty = annotatedProperty;
		}
	}

	/**
	 * A type that is neither registered explicitly, nor mentioned in any annotation-mapped property,
	 * nor used by any bridge, but is mentioned in an programmatically mapped property.
	 * It should be automatically discovered when contributing the programmatic mapping;
	 * if it isn't, the field "nonAnnotationMappedEmbedded.text" will be missing.
	 */
	@Embeddable
	@Indexed(index = "SHOULD_NOT_BE_INDEXED")
	public static class NonExplicitlyRegisteredNonAnnotationMappedType {
		@Field
		private String text;

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
	}

}
