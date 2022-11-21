package Server.AbstractObjects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
public class FileObject extends ByteObject {
    private String fileName;
    private byte[] file;
}
