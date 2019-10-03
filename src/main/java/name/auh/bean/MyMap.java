package name.auh.bean;

import java.util.HashMap;
import java.util.Map;

public class MyMap<T, V> {

    public Map<T, V> map = new HashMap<T, V>();

    public MyMap<T, V> kv(T k, V v) {
        map.put(k, v);
        return this;
    }


}
