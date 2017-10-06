
## Clojure-Java Interop by Example

I've been writing a lot of Clojure for almost two years now, but I never actually learned Java until recently, and primarily for the purpose of being able to use Java libraries in Clojure.

Until actually learning Java, I found interop the most difficult part of Clojure, mainly because Java has a lot of mental complexity that you need to take on---i/o, for example, tends to involve all kinds of BufferedThis and StreamingThat and casting across like 5 different types before you can do anything, and most tutorials on Clojure-Java interop assume you know that stuff, and also assume you know, e.g., what a static method is, and that a "class" is the name for both the basic organization of java source code *and* the name for the compiled output files.

Also, the existing tutorials seem to be mostly limited to telling you how to instantiate Java objects and call methods on them, they don't really focus on stuff like "what's a method" or "how do I use a library from Maven." Here's [a very good and delightfully brief example of a tutorial like that](http://xahlee.info/clojure/clojure_calling_java.html).

So, partly to solidify my own growing understanding and partly to help out others who might be in a similar position, here's a quick example/mini-tutorial of working Clojure-Java interop that tries to assume pretty close to zero knowledge of Java, and only a basic working knowledge of Clojure.

The task will be to generate a thumbnail of an image from the command line. At the end, we want to be able to type "java -jar ourImage target.jpg" and get a thumbnail.  

First, we'll do it in Java, and then we'll do it in Clojure. All the code is [housed on github](https://github.com/paultopia/interop-demo).

### Making a Thumbnail in Java

We'll use the [imgscalr](https://github.com/rkalla/imgscalr) library, which, although it hasn't been updated in a while, seems to be widely recommended. (**Warning:** don't click the external links on that github: last I checked the domains seem to have been hijacked.) We'll also use the [Gradle](https://gradle.org/) build tool, because Maven uses XML and XML is agony. 

First, install Gradle from the instructions at the link above.  Then we'll need a build.gradle file to set up the process.  Here's my build.gradle: 

```groovy
plugins {
id 'com.github.johnrengelman.shadow' version '2.0.1'
id 'java'
}

jar {
    manifest {
        attributes 'Main-Class': 'thumbnail.Thumbnail'
    }
}

dependencies {
             compile 'org.imgscalr:imgscalr-lib:4.2'    
}

repositories {
             mavenCentral()          
}
```

The first line is [a plugin](http://imperceptiblethoughts.com/shadow/) that allows us to create an *uberjar* --- one big jar file (a jar is essentially a tar.gz of compiled java files---"classes" + resources with a funny extension) with our code plus library code. Even though this is built into Leiningen for Clojureland, it isn't the default for Javaland.

Because we'll be using a library, and we don't want to mess around with putting that library on an explicit classpath, an uberjar will be much more convenient. Honestly, I haven't quite figured out how to use a library jar that isn't bundled into an uberjar yet.  But with this plugin we can just use `gradle shadowjar` to build and everything will *just work*.

The second line is the plugin to build Java projects. It turns out that Gradle can be used to compile lots of things, though I think it's mostly used for Java and Groovy (I think that this config file is written in Groovy, though I'm not sure)... and maybe Kotlin?

The jar/manifest block will tell Java where to find out main class, which is the entry point to the application. Java applications are organized into packages composed of classes, so this says "go to the thumbnail package and the Thumbnail class, and you'll find the main method."

The rest of that file should be pretty self-explanatory.  

Our only other file is src/main/java/Thumbnail.java, which is as follows: 

```java
package thumbnail;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import org.imgscalr.Scalr;

public class Thumbnail {

    public static BufferedImage readImageFile(String filename) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(filename));
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return image;
    }

    public static BufferedImage scale(BufferedImage inImage){
        BufferedImage outImage = Scalr.resize(inImage, 200);
        return outImage;
    }

    public static void main(String[] args) {
        String inFile = args[0];
        String outFile = args[1];
        try {
            BufferedImage inImage = readImageFile(inFile);
            BufferedImage outImage = scale(inImage);
            ImageIO.write(outImage, "jpg", new File(outFile));
        }
        catch (IOException e){
            System.err.println(e.getMessage());
        }
    }
}
```

Let's break this down.  

First we have our package declaration, which just helps the compiler organize the code.

Then we have a bunch of imports.  We're importing the built-in types (classes) BufferedImage and File which we need to use to read in and write out our data. Java is statically typed, and, like I said, i/o tends to involve lots of casts.  We're also importing some useful IO classes --- imageIO is a built-in class to handle, unsurprisingly, image i/o, 

We also have to import the exception that we're going to have to handle, because [Java makes you explicitly throw or handle almost all exceptions](https://docs.oracle.com/javase/tutorial/essential/exceptions/catchOrDeclare.html). 

Finally, we have to import the library that we're using. It provides one class that we need, Scalr, and we'll just be using one static method from that class, `Scalr.resize()`.  

FYI, a *static method* is a method that you can call like a normal function, that's attached to the name of a class. The alternative is an *instance method*, which is a method that is attached to an instance of a class. For example, if we have a `String` object named `stringeyMcStringFace` then to get the number of characters we'd call the instance method `stringeyMcStringFace.length()`.  We can't call it as a static method like `String.length(stringeyMcStringFace)` --- that would fail to compile.

Finally, we create our class, and create a bunch of static methods to hold different pieces of functionality. (This could actually just all be crammed into the main method pretty easily, but I broke it up for readability.)

The first method, `readImageFile` is just a helper method for i/o. It declares and instantiates a `BufferedImage`, and then it tries to read a file with the filename it's passed. Because this can throw an `IOException`, we have to wrap it in a try/catch. Also note how we have to explicitly instantiate a `File` object to pass to `ImageIO.read()`, which returns our `BufferedImage`.  This is Java, it's gonna be verbose.

The second method just wraps the method we got from imgscalr. It's mostly in there for comparision with the equivalent Clojure code in a bit. (Incidentally, the second argument to the `resize` method is the maximum size of our output image.)

The last method is our main method.  It's the entry point that will be called when we call it from the command line, and it always has the same signature and name in every Java program. It's pretty self-explanatory: we read the filenames off the commandline, then we to read the file in there, scale it, and write it back out.

That's it!  If we compile with `gradle shadowjar` this will give us jar file, and then if we go to the root directory of the repo (that contains the sample image) and run `java -jar javathumbnail/thumbnail/build/libs/thumbnail-all.jar bigkitty.jpg smallkitty-java.jpg` we should get a very cute small kitty!

Let's look at the equivalent in Clojure!

### Making a Thumbnail in Clojure

Instead of Gradle now we'll use Leiningen. Here's our project.clj:

```clojure
(defproject thumbnail "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.imgscalr/imgscalr-lib "4.2"]]
  :main ^:skip-aot thumbnail.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

```

Because I'm assuming some basic Clojure knowledge, I won't go into too much detail here, but not that you can fetch a Java library off of Maven Central in exactly the same way as you'd fetch a Clojure library off of Clojars. Leiningen makes it seamless.

The only other file we need is our src/thumbnail/core.clj:

```clojure
(ns thumbnail.core
  (:gen-class)
  (:require [clojure.java.io :as io])
  (:import [org.imgscalr Scalr]
           [java.awt.image BufferedImageOp]
           [javax.imageio ImageIO]))

(defn read-image [filename]
  (ImageIO/read (io/file filename)))

(defn scale [image]
  (Scalr/resize image 200 (into-array BufferedImageOp [])))

(defn -main
  [infile outfile]
  (let [image (read-image infile)
        scaled (scale image)]
    (ImageIO/write scaled "jpg" (io/file outfile))))

```

Again, I'm assuming you already know Clojure, so I'm only going to explain the interop bits.

First, note that in addition to `(:require)` in our ns declaration we also have `(:import)`. The format of an import is that we give it the java package identifier and then all classes, separated by spaces.  So if we wanted to import multiple classes from `java.awt.image` we could do an import like `[java.awt.image BufferedImageOp BufferedImage]`. (Incidentally, I'll explain this `BufferedImageOp` business in a minute).

We'll also want to require the `clojure.java.io` built-in namespace, which wraps some of that Java verbosity. Note, however, that we do not need to import errors---we're in Clojure now, we don't have to explicitly catch them!---and we don't need to import the types we're using either, Clojure will just let us use reflection to sort them out.

Thus, `read-image` is a much less verbose version of `readImageFile` from the Java side that does exactly the same thing: `io/file` [returns a Java File](https://clojure.github.io/clojure/clojure.java.io-api.html#clojure.java.io/file) which we can then pass straight to the static `read()` method of the `ImageIO` object by calling `ImageIO/read` just as if it were a clojure function. 

Likewise, `scale` is pretty straightforward: we call the static method from the imported library just like we called the method from the Java stdlib a moment ago. It's really that easy. 

There is one little glitch at the end, though. See, it turns out that if we actually look at [the signature for the resize method](https://github.com/rkalla/imgscalr/blob/2fac6c4c0d0e857d31daeed1e670c937e2c7ef70/src/main/java/org/imgscalr/Scalr.java#L1124), it actually has three parameters. The third is a [varargs](https://docs.oracle.com/javase/tutorial/java/javaOO/arguments.html) parameter, which means it takes an array of objects of type `BufferedImageOp`.  

We don't actually need to give that parameter anything for our simple example, and the Java compiler is kind enough that it will let us leave off a varargs parameter entirely if we don't need it. Unfortunately, the Clojure compiler is not so generous (this might be the only case in human history where Clojure is more verbose than Java), and makes us explicitly pass these arguments in.

Accordingly, we have to construct an empty array that would have the Java type `BufferedImageOp[]` and pass that into `resize` as the third argument.  The [into-array](http://clojuredocs.org/clojure.core/into-array) function in clojure.core provides that capacity.

The main function is self-explanatory after all that---it's just a direct translation of method calls to Clojure function calls.

And we're done!  Running `java -jar clojurethumbnail/thumbnail/target/uberjar/thumbnail-0.1.0-SNAPSHOT-standalone.jar bigkitty.jpg smallkitty-clojure.jpg` will give us the same cute kitty as before!

Resources: 

[A real-world example of using imgscalr from Clojure](http://www.flyingmachinestudios.com/programming/refactoring-to-datamappers-in-clojure/)

[Another more complicated example of using imgscalr from Clojure](https://gist.github.com/jkk/3959731)

[StackOverflow answer explaining the varargs quirk](https://stackoverflow.com/a/18501367/4386239)
