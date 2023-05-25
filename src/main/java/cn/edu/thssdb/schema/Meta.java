package cn.edu.thssdb.schema;

import cn.edu.thssdb.exception.CustomIOException;
import cn.edu.thssdb.exception.MetaFileNotFoundException;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Meta {
    private String folder_name;

    private String file_name;

    private String full_path;

    /** [method] 创建元数据文件，以文本形式存储 */
    public Meta(String folder_name, String file_name) {
        this.folder_name = folder_name;
        this.file_name = file_name;
        this.full_path = Paths.get(folder_name, file_name).toString();

        File d = new File(folder_name);
        if (!d.isDirectory()) {
            d.mkdirs();
        }
        File f = new File(this.full_path);
        if (!f.isFile()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                throw new CustomIOException();
            }
        }
    }

    /** [method] 从数据文件中读取元数据 */
    public ArrayList<String[]> readFromFile() {
        ArrayList<String[]> lines = new ArrayList<String[]>();
        String str;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(full_path));
            while ((str = reader.readLine()) != null) {
                lines.add(str.split(" "));
            }
            reader.close();
        } catch (FileNotFoundException e) {
            throw new MetaFileNotFoundException();
        } catch (IOException e) {
            throw new CustomIOException();
        }
        return lines;
    }

    /** [method] 写入元数据到指定路径 */
    public void writeToFile(ArrayList<String> meta_data) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(full_path));
            for (String line : meta_data) {
                writer.write(line);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            throw new CustomIOException();
        }
    }

    /** [method] 删除元数据文件 */
    public void deleteFile() {
        File f = new File(this.full_path);
        f.delete();
    }
}