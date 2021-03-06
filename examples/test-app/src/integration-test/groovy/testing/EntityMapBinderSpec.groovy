package testing

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import repoapp.Address
import repoapp.Org
import spock.lang.Issue
import spock.lang.Specification

@Integration
@Rollback
class EntityMapBinderSpec extends Specification {

    @Issue("https://github.com/yakworks/gorm-tools/issues/181")
    void "perform gormtools binding after grails binding"() {
        setup:
        Map params = [name:"test-org", address:[city:"Rajkot", testId:"100"]]

        when: "Address is bound as part of org association binding"
        Org org = new Org()
        org.properties = params

        then:
        org.hasErrors() == false
        org.name == "test-org"
        org.address != null
        org.address.city == "Rajkot"

        when: "now try to bind just address"
        Address address = new Address()
        address.bind params.address

        then:
        address != null
        address.city == "Rajkot"
    }
}
