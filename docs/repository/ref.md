
## The Repository Artefact

This plugin adds a new artefact type **Repository**. Each domain will have a spring bean setup for it if 
one doesn't exists already.

A repository bean is configured for each domain with a [DefaultGormRepo][]{.new-tab} unless explicit repository class.   
The trait [GormRepo][]{.new-tab} implements the [RepositoryApi][]{.new-tab} interface and is what backs the DefaultGormRepo. 
You'll mostly use the [GormRepo][]{.new-tab} trait when creating a custom concrete implementation of a Repository.

Reference to a Repository for given domain class can be easily obtained by calling `MyDomainClass.repo` static method.

### Implementing A Repository

If you need to override the [DefaultGormRepo][] that is attached to each domain then you can create your own service
inside grails-app/repository and name it ```YourDomainNameRepo``` (eg ```OrgRepo```). 
Plugin will automatically lookup all Repository classes and configure them as spring service beans to be used for 
your domain.

A Repository must either implement [GormRepo][] Trait or if you wish extend [DefaultGormRepo][]
 
**Example:**
 
 ```groovy
 class OrgRepo implements GormRepo<Org> {
     
     void beforeBind(Org org, Map params) {
        //do some thing before create
      }
      
 }
 ```

## GormRepoEntity Trait

See Groovydocs api for the [GormRepoEntity][] that is injected onto all domains.

### Instance methods added to the domains

Every domain gets a repository which is either setup for you or setup by implementing 
[GormRepo][] 
Each method is transactional and will prevent incomplete cascading saves.

- **persist()**: calls the GormRepo's persist which in turn calls domain.save(failOnError:true) 
  Throws a [EntityValidationException][EntityValidationException]
  
- **remove()**:  calls the GormRepo's remove. 
  Throws a [EntityNotFoundException][EntityNotFoundException]
  
### Statics added to the domain

- **create(params)**:  calls the repo.create which does the bolier plate code you might find in a scaffolded controller. 
creates a new instance, sets the params and calls the repository.save (essentially the persist()). **ex:** `Book.insertAndSave([name:'xyz',isbn:'123'])`
Throws a [EntityValidationException][EntityValidationException] if anything goes wrong

- **update(params)**:  calls the repo.update which does the boiler plate code you might find in a scaffolded controller. gets the instance base in the params.id, sets the params and calls the repository.save for it. **ex:** Book.update([id:11,name:'aaa']) Throws a [EntityValidationException] if anything goes wrong

- **remove(id)**:  calls the repository.removeById gets the instance base in the params.id, calls the delete for it. **ex:** `Book.remove([id:11])`
Throws a [EntityNotFoundException][EntityNotFoundException] if anything goes wrong

- **repo**: a quick way to get to the repository for the Domain. It will return the DefaultGormRepo that was auto created 
  or one you defined for the domain under grails-app/repository.


## Repository Events 

### Methods

Each Repository can implement any of the methods listed below and they will get called during persistence operation.  
 
- **beforeBind(T instance, Map params)** - Called before a new instance is saved, can be used to do custom data binding or initialize the state of domain etc.  
- **afterCreate(T instance, Map params)** - Called after the new instance is saved.  
- **beforeRemove(T instance)** - Called before an instance is deleted. Can be utilized to cleanup related records etc.  
- **afterRemove(T instance)** - After an instance is removed.  
- **beforeUpdate(T instance, Map params)** - Called before an instance is updated  
- **afterUpdate(T instance, Map params)** - Called after an instance is updated  
- **beforePersist(T instance)** - Called every time before an instance is saved.  
- **afterPersist(T instance)** - Called every time after an instance is saved.
  
### Spring Events

The Repository also publishes a number of 
[events as listed in the Groovydoc API](https://yakworks.github.io/gorm-tools/api/gorm/tools/repository/events/package-summary.html)

**Example**  
```groovy
import org.springframework.context.event.EventListener
import gorm.tools.repository.events.BeforeBindEvent

class OrgListener {
   
    @EventListener
    void beforeBind(beforeBindEvent<Org> event) {
       Org org = event.entity
       //Do some thing here.
    }
}

```

### Grails Events

The Repository also provides a possibility to handle events using Grails annotations. Please see docs for [Grails Events]{.new-tab}.

#### Publishing events
Grails provides two ways for creating events - using [@Publisher]{.new-tab} annotation on a method and using
[EventBus]{.new-tab} directly, please see docs for [Event Publishing]{.new-tab}.
In case of using publisher annotation Grails takes event id from the method name (method with [@Publisher]{.new-tab} annotation).
If using [EventBus]{.new-tab} we should specify event id manually.

By default Repository uses [EventBus]{.new-tab} to create events (see [RepoEventPublisher]{.new-tab}).
It publishes a number of [Repository Events]{.new-tab} and provides it's own way to build event ids.
All ids of repository events correspond to the format ```<domainName>.<eventTypeName>```.
As we can see there are two values separated with a dot. The first comes a name of a domain class, for which an event
is created and the second - a type of a specific repository event. For example, in case we call ```persist()``` method
on a domain entity called ```Org```, the repository invokes several events, one of them is BeforePersist event with id ```Org.beforePersist```.

#### Subscribing to events
Grails provides several options for handling events, please see Grails docs for [Event Handling].

In case of adding [@Subscriber]{.new-tab} annotation to a method, Grails determines event id from the method name
by default. For example, methods like ```someEvent()``` or ```onSomeEvent()``` listen to the event with id ```someEvent```.

Due to the fact that ids of repository events contain ``` . ``` symbol, we should pass event id to the Subscriber annotation like so:

```groovy
    @Subscriber("SomeDomain.someEvent")
    void someMethod() {}
```

According to Grails docs, a class which contains a listener method (with [@Subscriber]{.new-tab} annotation) should be a **spring bean**.

Please see the example with ```OrgSubscriber``` below:

**Example**
```groovy
import grails.events.annotation.Subscriber
import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.repository.events.AfterPersistEvent

class OrgSubscriber {
   
    @Subscriber("Org.beforePersist")
    void beforePersist(BeforePersistEvent event) {
       // ...
    }
    
    @Subscriber("Org.afterPersist")
    void afterPersist(AfterPersistEvent event) {
       // ...
    }
}

```
In this example we can see two listeners which handle events that occur before and after
persisting an entity of the Org domain class.

> :memo: 
Calling methods which trigger events inside an event listener causes an infinite loop

## Using external groovy beans as event listeners.
[Spring dynamic languages support](https://docs.spring.io/spring/docs/current/spring-framework-reference/languages.html#groovy) 
can be used to register classes defined outside of application into groovy scripts as spring beans.
Which makes it possible to externalize the event listeners if required so.
 
Here's an example.

 ```SomeEventListener.groovy``` outside of grails app.
 
```groovy
 
 
import gorm.tools.repository.events.AfterRemoveEvent
import gorm.tools.repository.events.BeforeBindEvent
import org.springframework.context.event.EventListener

public class SomeEventListener {

    @EventListener
    void beforeBind(BeforeBindEvent<Org> event) {
        Org org = event.entity
        if(event.bindAction == 'Create'){
            //do something before create with event.data
            org.event = "Creating with data ${event.data}"
        } else if(event.bindAction == 'Update'){
            org.event = "Updating with data ${event.data}"
        }
    }
}

```

 Define external class a spring bean in resources.groovy

 File ```grails-app/conf/spring/resources.groovy```
 
```groovy

    File file = new File("path to RepoEventListener.groovy")
    xmlns lang: "http://www.springframework.org/schema/lang"
    
    String beanName = GrailsNameUtils.getPropertyName(file.name.replace('.groovy', ''))
    lang.groovy(id: beanName, 'script-source': "file:<path to file>", 'refresh-check-delay': 1000)

```
 
See [example](https://github.com/yakworks/gorm-tools/blob/8356c50e13874921c9b42c2c9fa1f93d2c2a6826/examples/benchmarks/grails-app/conf/spring/resources.groovy#L25-L25) 
in benchmarks project.  


## Data binding using MapBinder
Plugin comes with a ```MapBinder``` Which is used by a Repository to perform databinding.
Plugin configures ```entityMapBinder``` as default implementation of ```MapBinder```. ```entityMapBinder``` is similar 
to grails data binder in the sense that it uses registered value converters and fallbacks to spring ConversionService.
However entityMapBinder is optimized to convert most commonly encountered property types such as Numbers and Dates 
without going through the converters, thus resulting in faster performance.

**Example**

```groovy
class SomeService {
    @Autowired
    MapBinder binder

    void foo(Map params) {
        Org org = new Org()
        binder.bind(org, params)
    }
}

```

**Using custom MapBinder**  
By default all Repositories use the default ```entityMapBinder``` for databinding. However when a Repository is explicitly 
created for a domain class, and if required, a custom MapBinder implementation can be used to perform databinding as per the need.

```groovy

class CustomMapBinder implements MapBinder {

    public <T> GormEntity<T> bind(GormEntity<T> target, Map<String, Object> source, String bindMethod) {
        //implement  
    }

    public <T> GormEntity<T> bind(GormEntity<T> target, Map<String, Object> source) {
        //implement
    }

}
```
then register the bean 

```java
beans = {
    customMapBinder(CustomMapBinder) 
}
```

```groovy
class OrgRepo implements GormRepo<Org> {
    
    @Autowired
    @Qualifier("customMapBinder")
    CustomMapBinder mapBinder
    
    .........   
}

```

This will make the OrgRepo use CustomMapBinder for data binding.

## RepoUtil, RepoMessage Helpers

See [RepoUtil]

#### RepoUtil:

**checkFound(entity, Map params,String domainClassName)** checks does the entity exists, if not throws [EntityNotFoundException] with human readable error text

**checkVersion(entity,ver)** checks the passed in version with the version on the entity (entity.version) make sure entity.version is not greater, throws OptimisticLockingFailureException

**flush()** flushes the session

**clear()** clears session cache

**flushAndClear()** flushes the session and clears the session cache

#### RepoMessage contains bunch of help methods for creating text messages

See [RepoMessage]

The example below shows how to build ```saved``` message for a domain:

```groovy

    User user = new User(id:100,version:1)

    Map msg = RepoMessage.saved(user)
    assert 'default.saved.message' == msg.code //i18 code
    assert 100 == msg.args[1]

```

List of available messages

* saved
* not saved
* updated
* not updated
* deleted
* not deleted
* notFound
* optimisticLockingFailure - Another user has updated the resource while you were editing

Gorm-tools provides its own types of exceptions to handle errors which relate to domains.

## EntityValidationException
See [EntityValidationException][EntityValidationException]

An extension of the default ValidationException. It is possible to pass the entity and the message map.

## EntityNotFoundException
See [EntityNotFoundException][EntityNotFoundException]

An extension of the EntityValidationException to be able to handle rest request which should respond with 404 error.


## Async batch processing support
Plugin makes it easy to process list of batches asynchronously with transaction using [AsyncBatchSupport](https://yakworks.github.io/gorm-tools/api/gorm/tools/async/AsyncBatchSupport.html). 
[GparsBatchSupport](https://yakworks.github.io/gorm-tools/api/gorm/tools/async/GparsBatchSupport.html) is default implementation provided by the plugin.


**batchSize** - Is the batchsize used for slicing the list. The default value is obtained from ```hibernate.jdbc.batch_size``` configuration setting. However it can be explicitely passed in args as shown in below example.  
**poolSize** - Is the size of Gpars thread pool used by ```GparsBatchSupport```. The default value can configured using ```gpars.poolsize```. If not configured, it will use the default poolsize used by Gpars. which is available processors + 1


**Example**:
```groovy

class TestService {
    AsyncBatchSupport asyncBatchSupport

    void insertBatches(List<Map> list) {
        asyncBatchSupport.parallelCollate([batchSize:100], list) { Map record, Map args ->
            Org.create(record)
        }
    }

}

```

The above code snippet will slice the list into batches of 100 and run each batch in parallel and wrap it in transaction. 

The list can be processed in parallel without it being wrapped in transaction using ```asyncBatchSupport.parallel``` method.

```groovy

asyncBatchSupport.parallel(asyncBatchSupport.collate(list)) { List batch, Map args ->
    //do some thing with the batch.
}

```



## Testing support
Plugin provides [GormToolsTest][] and [GormToolsHibernateSpec][]
To make it easy to write tests which utilizes repository.

**Writing unit tests using GormToolsTest**  
GormToolsTest extends grails DataTest and configures a repository bean for every mock domain.  
The repository class must exist in same package as the domain class, or else, it will configure 
DefaultGormRepo as the repository for the given domain.
  
```groovy

class CitySpec extends Specification implements GormToolsTest {
   
   void setup() {
     mockDomain(City)
   }
   
   void "test create"() {
     given:
     Map params = [name:"Chicago"]
     
     when:
     City city = City.create(params)
     gormtools
     
     then:
     city.name == "Chicago"
   }
}
```  

**GormToolsHibernateSpec**  
GormToolsHibernateSpec extends HibernateSpec and setups repository beans for domains. 
Can be used to unit test with full hibernate support with inmemory database.


```groovy

class CitySpec extends GormToolsHibernateSpec {
   
  List<Class> getDomainClasses() { [City] }
   
   void "test create"() {
     given:
     Map params = [name:"Chicago"]
     
     when:
     City city = City.create(params)
     //or City.repo.create(params)
     
     then:
     city.name == "Chicago"
   }
}
```  

When ```getDomainClasses()``` is overridden GormToolsHibernateSpec will try to find the repository in the same package as domain class. 
Alternatively if ```getPackageToScan()``` is provided, it will find all the repository from the given package and below it. 


[RepositoryApi]: https://yakworks.github.io/gorm-tools/api/gorm/tools/repository/RepositoryApi.html
[GormRepo]: https://yakworks.github.io/gorm-tools/api/gorm/tools/repository/GormRepo.html
[GormRepo source]: https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/gorm/tools/repository/GormRepo.groovy
[DefaultGormRepo]: https://yakworks.github.io/gorm-tools/api/gorm/tools/repository/DefaultGormRepo.html
[GormRepoEntity]: https://yakworks.github.io/gorm-tools/api/gorm/tools/repository/GormRepoEntity.html
[GormRepoEntity source]: https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/gorm/tools/repository/GormRepoEntity.groovy
[Gorm]: http://gorm.grails.org/latest/hibernate/manual/index.html
[EntityValidationException]: https://yakworks.github.io/gorm-tools/api/gorm/tools/repository/errors/EntityValidationException.html
[EntityNotFoundException]: https://yakworks.github.io/gorm-tools/api/gorm/tools/repository/errors/EntityNotFoundException.html
[GormToolsTest]: https://yakworks.github.io/gorm-tools/api/gorm/tools/testing/GormToolsTest.html
[GormToolsHibernateSpec]: https://yakworks.github.io/gorm-tools/api/gorm/tools/testing/GormToolsHibernateSpec.html
[Repository Events]: https://yakworks.github.io/gorm-tools/api/gorm/tools/repository/events/package-summary.html
[RepoEventPublisher]: https://yakworks.github.io/gorm-tools/api/gorm/tools/repository/events/RepoEventPublisher.html
[RepoUtil]: https://yakworks.github.io/gorm-tools/api/gorm/tools/repository/RepoUtil.html
[RepoMessage]: https://yakworks.github.io/gorm-tools/api/gorm/tools/repository/RepoMessage.html
[RepoMessage]: https://yakworks.github.io/gorm-tools/api/gorm/tools/repository/RepoMessage.html

[Grails Events]: http://async.grails.org/latest/guide/index.html#events
[Event Publishing]: http://async.grails.org/latest/guide/index.html#notifying
[Event Handling]: http://async.grails.org/latest/guide/index.html#consuming
[@Subscriber]: http://async.grails.org/latest/api/grails/events/annotation/Subscriber.html
[@Publisher]: http://async.grails.org/latest/api/grails/events/annotation/Publisher.html
[EventBus]: http://async.grails.org/latest/api/grails/events/bus/EventBus.html 