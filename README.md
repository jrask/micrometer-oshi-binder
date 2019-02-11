# Micrometer-oshi-binder

Micrometer.io binder for Operating System & Hardware Information (oshi)
based on http://github/oshi/oshi



## Memory Metrics

```java

new MemoryMetrics().bindTo(yourRegistry);

```



## Processor Metrics

CPU usage from processor metrics can be sampled in two ways and you can use both or one of them.
This depends on your monitoring system because in this case it not possible for any MeterRegistry to handle
this.

* CPU Percent (think telegraf, statsd)
* CPU Seconds total (think prometheus)


```java

new ProcessorMetrics().bindTo(yourRegistry);

new ProcessorMetrics(ProcessorMetrics.CpuSampleType.PERCENT|SECONDS_TOTAL[ALL)
    .bindTo(yourRegistry);

```

## Network Metrics

```java

new NetworkMetrics().bindTo(yourRegistry)

```


### Sample output

These are outputs from the RegistryExample program that you can find in the code and
it uses SimpleMeterRegistry and StepMeterRegistry.

#### SimpleMeterRegistry



```
        system.memory.available, value=6.735171584E9, []
system.network.packets.received, count=418034.0, []
                 system.load.1m, value=3.00927734375, [tag(cpus=2)]
                 system.load.5m, value=3.9501953125, [tag(cpus=2)]
       system.cpu.seconds.total, count=1056.0, [tag(mode=system)]
    system.network.packets.sent, count=94115.0, []
  system.network.bytes.received, count=6.02688512E8, []
       system.cpu.seconds.total, count=0.0, [tag(mode=irq)]
       system.cpu.seconds.total, count=7527.0, [tag(mode=idle)]
       system.cpu.seconds.total, count=2633.0, [tag(mode=user)]
      system.network.bytes.sent, count=8622080.0, []
       system.cpu.seconds.total, count=0.0, [tag(mode=steal)]
       system.memory.swap.total, value=0.0, []
           system.cpu.usage.pct, value=14.935064935064934, [tag(mode=system)]
                system.load.15m, value=5.2099609375, [tag(cpus=2)]
             system.memory.used, value=1.04446976E10, []
           system.cpu.usage.pct, value=0.0, [tag(mode=irq)]
           system.cpu.usage.pct, value=61.938061938061935, [tag(mode=idle)]
           system.cpu.usage.pct, value=0.0, [tag(mode=nice)]
           system.cpu.usage.pct, value=23.126873126873125, [tag(mode=user)]
            system.memory.total, value=1.7179869184E10, []
        system.memory.swap.used, value=0.0, []
       system.cpu.seconds.total, count=0.0, [tag(mode=softirq)]

```


#### StepMeterRegistry

```
        system.memory.available, value=6.735171584E9, []
system.network.packets.received, count=666.0, []
                 system.load.1m, value=3.00927734375, [tag(cpus=2)]
                 system.load.5m, value=3.9501953125, [tag(cpus=2)]
       system.cpu.seconds.total, count=6.0, [tag(mode=system)]
    system.network.packets.sent, count=657.0, []
  system.network.bytes.received, count=616448.0, []
       system.cpu.seconds.total, count=0.0, [tag(mode=irq)]
       system.cpu.seconds.total, count=25.0, [tag(mode=idle)]
       system.cpu.seconds.total, count=10.0, [tag(mode=user)]
      system.network.bytes.sent, count=112640.0, []
       system.cpu.seconds.total, count=0.0, [tag(mode=steal)]
       system.memory.swap.total, value=0.0, []
           system.cpu.usage.pct, value=14.935064935064934, [tag(mode=system)]
                system.load.15m, value=5.2099609375, [tag(cpus=2)]
             system.memory.used, value=1.04446976E10, []
           system.cpu.usage.pct, value=0.0, [tag(mode=irq)]
           system.cpu.usage.pct, value=61.938061938061935, [tag(mode=idle)]
           system.cpu.usage.pct, value=0.0, [tag(mode=nice)]
           system.cpu.usage.pct, value=23.126873126873125, [tag(mode=user)]
            system.memory.total, value=1.7179869184E10, []
        system.memory.swap.used, value=0.0, []
       system.cpu.seconds.total, count=0.0, [tag(mode=softirq)]
```
