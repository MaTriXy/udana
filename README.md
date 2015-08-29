# udana
Android lib that preserves any annotated field of activities over rotation.

###Usages 

Udana will preserve all `@Udana` annotated fields of an activity across rotations.

Udana can be used at the activity level, as follows :

```java
public class SampleActivity extends Activity {
    String string;
    @Udana String udanaString;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isPreserved = Uda.preserveMembers(this, savedInstanceState);
        System.out.println("string : " + string);
        System.out.println("udanaString : " + udanaString);
        string = "a";
        if (!isPreserved) {
            udanaString = "b";
        }
    }
}
```

Udana can also be used at the application leve and several overloads of the `Uda` class will provide you with more flexible ways to use Udana. Please read `Uda` static methods javadoc for more information.
