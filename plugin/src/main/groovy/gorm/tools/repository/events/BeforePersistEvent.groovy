package gorm.tools.repository.events

import gorm.tools.repository.api.RepositoryApi
import groovy.transform.CompileStatic

/**
 * Fired right before enitity save inside repository.persist
 */
@CompileStatic
class BeforePersistEvent<D> extends RepositoryEvent<D> {

    /** the args passed into persist */
    Map args

    BeforePersistEvent(RepositoryApi source, D entity, Map args) {
        super(source, entity, RepositoryEventType.BeforePersist.eventKey)
        this.args = args
        //setDataFromArgMap(args)
    }

    Map getData(){
        args ? args['data'] as Map : null
    }

    String getBindAction(){
        args ? args['bindAction'] as String : null
    }

}