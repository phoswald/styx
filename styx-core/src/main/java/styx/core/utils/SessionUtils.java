package styx.core.utils;

import java.util.ArrayList;
import java.util.List;

import styx.Value;

public final class SessionUtils {

    public static List<Value> filter(List<Value> list, Value after, Value before, Integer maxResults, boolean forward) {
        if(!forward) {
            Value dummy = after;
            after = before;
            before = dummy;
        }
        int pos = 0;
        int num = list.size();
        if(after != null) {
            while(num > 0 && list.get(pos).compareTo(after) <= 0) {
                pos++;
                num--;
            }
        }
        if(before != null) {
            while(num > 0 && list.get(pos + num - 1).compareTo(before) >= 0) {
                num--;
            }
        }
        if(maxResults != null && maxResults < num) {
            if(!forward) {
                pos += num - maxResults;
            }
            num = maxResults;
        }
        if(num < list.size()) {
            list = list.subList(pos, pos + num);
        }
        if(!forward) {
            List<Value> list2 = new ArrayList<>(num);
            for(int i = 0; i < num; i++) {
                list2.add(list.get(num - 1 - i));
            }
            list = list2;
        }
        return list;
    }
}
