# udana
Android lib that preserves any annotated field of activities over rotation.

###Usages 

Udana will preserve all `@Udana` annotated fields of an activity across rotations.

Udana can be used at the activity level, as follows :

```java
public class SampleActivity extends Activity {
    //not preserved
    String string;
    //preserved
    @Udana String udanaString;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isPreserved = Uda.preserveMembers(this, savedInstanceState);
        string = "a";
        
        //initialize Udana managed fields only if Udana didn't
        //initialize them (after a rotation).
        if (!isPreserved) {
            udanaString = "b";
        }
    }
}
```

Udana can also be used at the application leve and several overloads of the `Uda` class will provide you with more flexible ways to use Udana. Please read `Uda` static methods javadoc for more information.

### Why Udana ?

Android provides a way to share state between instances of an activity across rotation via the couple `onSaveInstanceState/onRestoreInstanceState`. 

However, the objects that can be passed between instances must be either primitives, serializable or parcelable. This restriction excludes active objects like threads, networks connections, etc.

Udana extends the default Android mechanism and allows you to pass *any objects* between instances across rotations (actually all config changes).

### Udana alternatives 

A traditional alternative to Udana is to use an invisible retained fragment to pass any objects between instances. These fragments are not recreated between rotations and they can be used to store objects of any type.

This process has been automated on Android by [memento](https://github.com/mttkay/memento).

However this process is heavy and requires fragment. Udana doesn't.

### Performances

Udana will help to save a lot of life cycles by avoiding to recreate stateful objects, which is already a performance factor for your apps. If the Android community demonstrates a significant interest for Udana, it can be optimized even more by using an annotation processor to preserve fields between instances.

Nevertheless, this approach would come with a price tag in term of methods added to your code, which can harm apps whose method count is close to the maximum supported by the dex format. Hence, for this first release, we favored introspection though it doesn't provide maximum speed.
