package Server.AbstractObjects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
public class DirectoryObject extends ByteObject {
    private String fileName;
    private String fileFolders;
    private byte[] file;
}
