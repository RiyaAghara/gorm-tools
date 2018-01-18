package gpbench.listeners

import gorm.tools.repository.events.BeforeBindEvent
import gpbench.CitySpringEvents
import gpbench.SecUtil
import groovy.transform.CompileStatic
import org.springframework.context.event.EventListener

import javax.annotation.ManagedBean

@ManagedBean
@CompileStatic
class CitySpringAnnotationEventListener {

    @EventListener
    void beforeBind(BeforeBindEvent<CitySpringEvents> event) {
        assert event.data
        //println "beforeBind on CitySpringEvents"
        CitySpringEvents entity = event.entity
        Long uid = SecUtil.userId
        Date dt = new Date()
        entity.createdBy = uid
        entity.editedBy = uid
        entity.createdDate = dt
        entity.editedDate = dt
    }

}