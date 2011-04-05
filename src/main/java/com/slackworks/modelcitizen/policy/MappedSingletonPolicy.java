package com.slackworks.modelcitizen.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slackworks.modelcitizen.CreateModelException;
import com.slackworks.modelcitizen.Erector;
import com.slackworks.modelcitizen.ModelFactory;
import com.slackworks.modelcitizen.erector.Command;
import com.slackworks.modelcitizen.field.MappedField;
import com.slackworks.modelcitizen.field.ModelField;
import com.slackworks.modelcitizen.template.BlueprintTemplateException;

/**
 * Enforce a @Mapped field in a @Blueprint as a Singleton {@link AbstractPolicy}
 * for creating models. 
 * 
 * If constructed with a Class, the first attempt to set @Mapped instance of Class
 * will use {@link ModelFactory#createModel(Class)} to create singleton to use 
 * for all instances of Class in registered {@link Blueprint}s. 
 * 
 * If constructed with a Model, the Model will be used for all @Mapped instances of
 * Model's class in registered {@link Blueprint}s.
 * 
 */
public class MappedSingletonPolicy implements FieldPolicy {

	private Logger logger = LoggerFactory.getLogger( this.getClass() );
	
	private Class singletonClass;
	private Object singleton;

	/**
	 * Create new Singleton with from a registered Class.
 	 *	 
	 * @param modelClass Class
	 */
	public MappedSingletonPolicy( Class singletonClass ) {
		super();
		this.singletonClass = singletonClass;
	}
	
	/**
	 * Create new instance with Model. 
	 * 
	 * @param model Object
	 */
	public MappedSingletonPolicy( Object model ) {
		super();
		
		this.singleton = model;
		this.singletonClass = this.singleton.getClass();
	}
	
	public Object getSingleton() {
		return singleton;
	}

	public void setSingleton(Object singleton) {
		this.singleton = singleton;
	}

	public Command process(ModelFactory modelFactory, Erector erector, ModelField modelField, Object model) throws PolicyException {
		
		logger.debug( "processing {} for {}", modelField, model );
		
		// If Model has not be set, create a new one from ModelFactory
		if ( modelField instanceof MappedField ) {
			if ( this.getSingleton() == null ) {
				logger.debug("  creating singleton for {}", this.getTarget() );
				try {
					this.setSingleton( modelFactory.createModel( this.getTarget(), false ) );
				} catch (CreateModelException e) {
					throw new PolicyException( e );
				}
			}
			
			// Set Singleton into model
			try {
				erector.getTemplate().set( model, modelField.getName(), this.getSingleton() );
			} catch (BlueprintTemplateException e) {
				throw new PolicyException(e);
			}
		}
		
		return Command.SKIP_INJECTION;
	}

	public Class getTarget() {
		return singletonClass;
	}
}