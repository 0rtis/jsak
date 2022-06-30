[![GitHub license](https://img.shields.io/github/license/0rtis/jsak.svg?style=flat-square)](https://github.com/0rtis/jsak/blob/master/LICENSE)
[![Nexus repo](https://img.shields.io/nexus/r/io.ortis/jsak?server=https%3A%2F%2Fs01.oss.sonatype.org%2F&style=flat-square)](https://s01.oss.sonatype.org/content/repositories/releases/io/ortis/jsak)
[![Follow @twitter handle](https://img.shields.io/twitter/follow/ortis95.svg?style=flat-square)](https://twitter.com/intent/follow?screen_name=ortis95) 


## JSAK - Java Swiss Army Knife

The Java Swiss Army Knife is a utility package that provide various tools for Java development

*You can support this project by donating to our crypto-currency wallet **0xA68fBfa3E0c86D1f3fF071853df6DAe8753095E2***

### Examples

#### [FormatUtils](https://github.com/0rtis/jsak/blob/master/src/main/java/io/ortis/jsak/FormatUtils.java)
Utility class for string formatting:
- Format bytes length
- Format duration
- Format exception


#### [Pointer](https://github.com/0rtis/jsak/blob/master/src/main/java/io/ortis/jsak/math/UZ.java)
Pointer of object (useful to access outer field from anonymous class)
```
Pointer<Integer> p = Pointer.of(42);
new Thread(){
    @Override
    public void run()
    {
        while(true){
            System.out.println(p.getValue());			
        }
    }
}.start();

while(true)
    p.setValue(p.getValue() + 1);
```

#### [UZ](https://github.com/0rtis/jsak/blob/master/src/main/java/io/ortis/jsak/math/UZ.java)
Unsigned integer
```
UZ.ONE.add(7).subtract(3).multiply(2)
```

#### [Bytes](https://github.com/0rtis/jsak/blob/master/src/main/java/io/ortis/jsak/io/bytes/Bytes.java)
Array of bytes as object (can be instanciated immutable) 

```
byte [] buffer = ...
Bytes bytes = Bytes.copy(buffer); //immutable
```

#### [BytesProvider](https://github.com/0rtis/jsak/blob/master/src/main/java/io/ortis/jsak/io/BytesProvider.java) & [BytesConsumer](https://github.com/0rtis/jsak/blob/master/src/main/java/io/ortis/jsak/io/BytesConsumer.java) 
Interfaces for agnostic I/O operation
```
InputStream inputstream = ...
BytesProvider provider = BytesProvider.of(inputstream);

OutputStream outputstream = ...
BytesConsumer consumer = BytesConsumer.of(outputstream);
```



#### [IOUtils](https://github.com/0rtis/jsak/blob/master/src/main/java/io/ortis/jsak/io/IOUtils.java)
Utility class for I/O operations:
- Read internal *resources*
- Copy N bytes from Path/RandomAccessFile to any BytesConsumer
- Copy at most N bytes or until the end of the stream (whichever come first) from Path/RandomAccessFile to any BytesConsumer
- Copy until the end of the stream from any BytesProvider to any BytesConsumer


#### [FileContentMonitor](https://github.com/0rtis/jsak/blob/master/src/main/java/io/ortis/jsak/io/file/FileContentMonitor.java)
Monitor change in one or more files
```
FileContentMonitor fileMonitor = new FileContentMonitor(Paths.get("path to my file"), Duration.ofMinutes(5));
new Thread(fileMonitor).start();
fileMonitor.addListener(new FileContentListener()
{
    @Override
    public void onFileContentChange(final Path path, final Bytes content)
    {
        System.out.println("File " + path + " has changed");
    }
});
```

#### [LogService](https://github.com/0rtis/jsak/blob/master/src/main/java/io/ortis/jsak/log/LogService.java)
Helper for the default Java log API
```
final LogService logService = new LogService().start();
logService.addListener(FilteredLogListener.CONSOLE_ALL);
final Logger logger = logService.getLogger("my logger");
logger.info("Hello world")
```

LogService can be configured from a JSON file through [LogConfigFileMonitor](https://github.com/0rtis/jsak/blob/master/src/main/java/io/ortis/jsak/log/config/LogConfigFileMonitor.java)
```
new LogConfigFileMonitor(logService, Paths.get("log_config.json"), null, Duration.ofSeconds(10), logService.getLogger("log-config")).start();
```

#### [Web server](https://github.com/0rtis/jsak/tree/master/src/main/java/io/ortis/jsak/http/server)
A simple & lightweight web server (based of SUN implementation) 
```
final HTTPServer httpServer = new HTTPServer(new InetSocketAddress("0.0.0.0", 8888), Executors.newFixedThreadPool(2), 2);
HTTPEndpoint endpoint = new HTTPEndpoint()
{
    @Override
    public boolean isMatch(final String requestMethod, final Map<String, List<String>> requestHeaders, final String path)
    {
        return true;
    }

    @Override
    public Response respond(final String requestMethod, final Map<String, List<String>> requestHeaders, final String path,
            final String query, final InputStream requestBody)
    {
        return new Response(null, 200, "hello world".getBytes(StandardCharsets.UTF_8), false);
    }
};
httpServer.addContext("/myPath", new HTTPRequestHttpHandler("/myPath", httpServerConfig, List.of(endpoint), Compression.Algorithm.Raw, new NoLimitHTTPLimiter(), 4096, logger));
```

#### [CachedIterator](https://github.com/0rtis/jsak/blob/master/src/main/java/io/ortis/jsak/collection/CachedIterator.java)
Get the current value of an Iterator more than once

```
final List<Integer> list = new ArrayList<>();
for (int i = 0; i < 256; i++)
    list.add(i);

final Iterator<Integer> iterator = list.iterator();
final CachedIterator<Integer> cachedIterator = CachedIterator.of(list);

int index = 0;
while (iterator.hasNext())
{
    final Integer value = iterator.next();
    Assert.assertEquals(value, cachedIterator.next());
    Assert.assertEquals(index++, cachedIterator.getIndex());
    Assert.assertEquals(value, cachedIterator.getCurrent());
}
```