# flink-spring
With this library you can build Flink jobs using Spring dependency injection framework.
Flink jobs can be build and set up using well known Spring mechanisms for dependency injection
making the implementation more clean, efficient and portable.

The goal of this library is **NOT** to run entire Flink job within Spring context.
Instead, we provide you with a helper classes that can be used in your Flink job to create Spring context
based on your Spring configuration classes and use this context to set up your pipeline. This can be done both on
Job Manager while processing `main` method (job submission phase) and also on Task Managers, for example in
`RichFunction::open` method. The created Spring context is short-lived.

This library provides all core Spring dependencies like:
- spring-context
- spring-beans
- spring-core
- spring-expression
- spring-aop

It is based on Spring version **5.3.27,** and it is compiled using Java **11**.

# How it works
The `flink-spring` library apart from providing Spring dependencies also provides a utility/registry `ContextRegistry` class.
This class has an API that allows you to load Spring context.
 
API Usage:
```java
DataStreamJob dataSteamJob = new DataStreamJob();
dataSteamJob = new ContextRegistry().autowiredBean(dataSteamJob, "org.example.config");
```

By calling `new ContextRegistry().autowiredBean(new DataStreamJob(), "org.example.config")` two things have happened:
1. The Spring context was created based on Spring configuration classes from `org.example.config` package.
2. All fields marked as `@Autowired` in `DataStreamJob` instance were injected by Spring. 

Additionally, the created Spring context was added to `ContextRegistry` instance scope registry.
thanks to this we can avoid recreating the context for every `.autowiredBean(...)` call.

# Usage in your code
1. Clone the repository and build it using
   ```shell
   mvn clean install
   ```

2. Copy created artifact `target/flink-spring-0.1.0-SNAPSHOT-jar-with-dependencies.jar` to `lib` folder
   of your Flink's distribution. Restart the cluster.

3. In your Flink job `pom.xml` add:
   ```xml
   <dependency>
      <groupId>com.getindata</groupId>
      <artifactId>flink-spring</artifactId>
      <version>0.1.0-SNAPSHOT</version>
      <scope>provided</scope>
   </dependency>
   ```
   Mind that the `scope` is set to `provided`. We don't want to include `flink-spring` lib in our job's uber jar.

# Example
In this example:
- sink is injected by Spring
- Source is not injected by Spring (it could) but instead we are passing a `EventProducer<Order>` object
to its constructor.

What we want to show here is that both, Flink components (Sources, Sinks etc.) as well as business code (`EventProducer<Order>`)
can be injected by this library.

For now, more detailed example can be found [here](https://github.com/kristoffSC/flink-using-springDI/tree/main/FlinkPipeline).

#### The main class
This class will have all its dependencies marked as `@Autowired` injected by Spring based on
configuration classes located in `org.example.config` package.

```java
package org.example;

import com.getindata.fink.spring.context.ContextRegistry;
import org.springframework.beans.factory.annotation.Autowired;
/* other imports omitted for clarity. */

public class DataStreamJob {

    // Will be injected by Spring based on Spring context configuration.
	@Autowired
	private EventProducer<Order> eventProducer;

    // Will be injected by Spring based on Spring context configuration.
	@Autowired
	private SinkFunction<SessionizeOrder> sink;

	public static void main(String[] args) throws Exception {
        // Using flink-spring library to inject DataStreamJob.class dependencies that are marked as
        // @Autowired. 
		new ContextRegistry()
			.autowiredBean(new DataStreamJob(), "org.example.config")
			.run(args);
	}

	private void run(String[] args) throws Exception {
		StreamExecutionEnvironment env = createStreamEnv();
		env.addSource(new CheckpointCountingSource<>(5, 5, eventProducer))
			.setParallelism(1)
			.process(new FlinkBusinessLogic())
			.setParallelism(2)
			.addSink(sink) // sink will be injected by Spring
			.setParallelism(2);

		env.execute("Flink Job  Powered By Spring DI.");
	}

	private static StreamExecutionEnvironment createStreamEnv() {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.getConfig().setRestartStrategy(RestartStrategies.noRestart());
		env.setRuntimeMode(RuntimeExecutionMode.STREAMING);
		env.enableCheckpointing(3000, CheckpointingMode.EXACTLY_ONCE);
		return env;
	}
}
```

#### Configuration classes
This is a Spring configuration class that can be used for loading Spring context by `flink-spring` library.

```java
package org.example.config;

// Spring libraries comes from flink-spring library
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/* other imports omitted for clarity. */

@Configuration
public class JobSpringConfig {

    @Bean
    public EventToStringConverter<SessionizeOrder> converter() {
        return event -> String.format("Order Details - %s", event.toString());
    }

    @Bean
    public SinkFunction<SessionizeOrder> sink(EventToStringConverter<SessionizeOrder> converter) {
        return new ConsoleSink<>(converter);
    }

    @Bean
    public EventProducer<Order> eventProducer() {
        return new OrderProducer();
    }

    @Bean
    public SessionManager sessionManager() {
        return new SimpleSessionManager();
    }

    @Bean
    public OrderProcessor<SessionizeOrder> orderProcessor(SessionManager sessionManager) {
        return new BusinessOrderProcessor(
            List.of(new SideNameAnonymization()),
            new OrderSessionize(sessionManager)
        );
    }
}

```




