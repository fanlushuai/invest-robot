package name.auh.bean;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Node {

    @NonNull
    private String xpath;

    private String data;

    @NonNull
    private String name;

    private Node sub;

    public Node setSub(Node node) {
        this.sub = node;
        return this;
    }


}
