package Server.AbstractObjects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor

public class StringObject extends ByteObject {
    public String message;
}
