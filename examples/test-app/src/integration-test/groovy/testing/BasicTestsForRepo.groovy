package testing

import gorm.tools.repository.RepoUtil
import gorm.tools.repository.errors.EntityValidationException
import spock.lang.Specification

class BasicTestsForRepo extends Specification {

    def repo

    void testSave() {

        def dom = new Jumper(name: "testSave")
        try {
            repo.save(dom)
            RepoUtil.flushAndClear()
            def dom2 = Jumper.findByName("testSave")
            assert dom2
        } catch (EntityValidationException e) {
            fail "Errors ${e.errors.allErrors[0]}"
        }
    }

    void testDelete() {

        def dom = new Jumper(name: "testDelete")
        try {
            repo.save(dom)
            RepoUtil.flushAndClear()
            def dom2 = Jumper.findByName("testDelete")
            repo.delete(dom2)
            def dom3 = Jumper.findByName("testDelete")
            dom3 == null
        } catch (EntityValidationException e) {
            fail "Errors ${e.errors.allErrors[0]}"
        }
    }

    void testInsert() {

        try {
            def result = repo.insert([name: "testInsert"])
            RepoUtil.flushAndClear()
            //println result
            assertTrue result.ok
            assertEquals "testInsert", result.entity.name
            assertEquals "default.created.message", result.message.code
            def dom2 = Jumper.findByName("testInsert")
            assert dom2.name == "testInsert"
        } catch (EntityValidationException e) {
            fail "Errors ${e.errors.allErrors[0]}"
        }
    }

    void testCreate() {

        try {
            def result = repo.create([name: "testInsert"])
            RepoUtil.flushAndClear()
            //println result
            assertTrue result.ok
            assertEquals "testInsert", result.entity.name
            assertEquals "default.created.message", result.message.code
            def dom2 = Jumper.findByName("testInsert")
            assert dom2.name == "testInsert"
        } catch (EntityValidationException e) {
            fail "Errors ${e.errors.allErrors[0]}"
        }
    }

    void testUpdate() {

        def dup = new Jumper(name: "testUpdate")
        dup.save()
        RepoUtil.flushAndClear()
        assert Jumper.findByName("testUpdate")
        try {
            def result = repo.update([id: dup.id, name: "testUpdateXXX"])
            RepoUtil.flushAndClear()
            //println result
            assertTrue result.ok
            assertEquals "testUpdateXXX", result.entity.name
            assertEquals dup.id, result.entity.id
            assertEquals "default.updated.message", result.message.code
            def dom2 = Jumper.findByName("testUpdateXXX")
            assert dom2.name == "testUpdateXXX"
        } catch (EntityValidationException e) {
            fail "Errors ${e.errors.allErrors[0]}"
        }
    }

    void testRemove() {

        def dup = new Jumper(name: "testRemove")
        dup.save()
        RepoUtil.flushAndClear()
        assert Jumper.findByName("testRemove")
        try {
            def result = repo.remove([id: dup.id])
            RepoUtil.flushAndClear()
            //println result
            assertTrue result.ok
            assertEquals dup.id, result.id
            assertEquals "default.deleted.message", result.message.code
            assertNull Jumper.findByName("testRemove")
        } catch (EntityValidationException e) {
            fail "Errors ${e.errors.allErrors[0]}"
        }
    }

}
