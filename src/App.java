import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class App {
    final static int KEYLED_COUNT = 0;
    final static int SOUNDS_COUNT = 1;
    final static int PROJECT_FILE_COUNT = 2;
    final static int PROJECT_NAME = 3;
    final static int PROJECT_AUTHOR = 4;
    final static int PROJECT_ENTRY = 5;
    final static int PROJECT_ROOT = 6;

    public static ZipInputStream zIS;

    public static void main(String[] args) {
        final String PACK1 = "C:\\Mobile\\Unipack\\Sideways.zip";
        final String PACK2 = "C:\\Mobile\\Unipack\\RISE (ft. The Glitch Mob, Mako, and The Word Alive).zip";
        final String PACK3 = "C:\\Mobile\\Unipack\\projetos teste.zip";
        final String OUTPUT = "C:\\Mobile\\Unipack\\outputs_zip";

        ZipFile mZipFile;
        Enumeration<? extends ZipEntry> enums;
        ZipEntry mZipEntry;

        Map<String, Map<Integer, Object>> projects;

        try {
            mZipFile = new ZipFile(PACK2);
            enums = mZipFile.entries();
            projects = new HashMap<>();

            String zip_file_name = getZipName(mZipFile);
            String project_name;
            String parent_folder_name;
            String file_name;
            String project;
            String parent_folder;
            String file;
            String root_project;
            File entry_file;
            File entry_file_name;

            int sound_count;
            int led_count;

            ZipEntry tmp_entry;

            System.out.println(zip_file_name);
            nextElement: while (enums.hasMoreElements()) {
                mZipEntry = enums.nextElement();
                entry_file = new File(mZipEntry.getName());
                file_name = entry_file.getName();
                parent_folder = entry_file.getParent();
                /*
                 * Definir as variáveis para facilmente detectar uma Unipack
                 */
                if (parent_folder == null) {
                    project_name = zip_file_name;
                    parent_folder_name = "";
                    root_project = "";
                    parent_folder = "";
                } else {
                    parent_folder_name = new File(parent_folder).getName();
                    project = new File(parent_folder).getParent();
                    parent_folder = parent_folder.replace(File.separator, "/");
                    root_project = parent_folder + "/";
                    System.out.println("root project: " + root_project);
                    if (mZipFile.getEntry(root_project + "Info") == null &&
                            mZipFile.getEntry(root_project + "info") == null) {
                        if (project == null) {
                            project_name = zip_file_name;
                            root_project = "";
                        } else {
                            project_name = project.substring(project.lastIndexOf(File.separator) + 1);
                            project = project.replace(File.separator, "/");
                            root_project = project + "/";
                        }
                    } else {
                        project_name = new File(root_project).getName();
                    }
                }
                System.out
                        .println("File: " + file_name + ", Parent: " + parent_folder_name + ", Project: " + project_name
                                + ", Project root: " + root_project);
                /*
                 * Não tenho interesse se for uma pasta. Caso seja um arquivo
                 */
                tmp_entry = (mZipFile.getEntry(root_project + "Info") != null)
                        ? mZipFile.getEntry(root_project + "Info")
                        : mZipFile.getEntry(root_project + "info");
                System.out.println(mZipEntry);
                if (mZipEntry.isDirectory()) {
                    continue nextElement;
                }
                /*
                 * Verifique se isto é um projeto Unipad para apresentar na explorador
                 */

                if (tmp_entry != null) {
                    if (!root_project.equals("") && !new File(root_project).getName().equalsIgnoreCase(project_name)) {
                        project_name = new File(root_project).getName();
                    }
                    if (projects.containsKey(project_name)) {
                        ((ArrayList<ZipEntry>) projects.get(project_name).get(PROJECT_ENTRY)).add(mZipEntry);
                    } else {
                        ArrayList<ZipEntry> entry_list = new ArrayList<>();
                        entry_list.add(mZipEntry);
                        Map<Integer, Object> project_properties = new HashMap<>();
                        InputStream mIS = mZipFile.getInputStream(tmp_entry);
                        InputStreamReader mISR = new InputStreamReader(mIS, "UTF-8");
                        Scanner mS = new Scanner(mISR);
                        String title = "";
                        String producerName = "";
                        String line;
                        while (mS.hasNextLine()) {
                            line = mS.nextLine().trim();
                            if (line.indexOf("title") == 0) {
                                title = line.substring(line.indexOf("=") + 1).trim();
                            } else if (line.indexOf("producerName") == 0) {
                                producerName = line.substring(line.indexOf("=") + 1).trim();
                            }
                            if (!title.equals("") && !producerName.equals(""))
                                break;
                        }
                        project_properties.put(PROJECT_NAME, (String) title);
                        project_properties.put(PROJECT_AUTHOR, (String) producerName);
                        project_properties.put(PROJECT_ROOT, (String) root_project);
                        project_properties.put(KEYLED_COUNT, (int) 0);
                        project_properties.put(SOUNDS_COUNT, (int) 0);
                        project_properties.put(PROJECT_FILE_COUNT, (int) 0);
                        project_properties.put(PROJECT_ENTRY, (ArrayList<ZipEntry>) entry_list);
                        projects.put(project_name, project_properties);
                    }
                }

            }
            System.out.println(projects.keySet());
            for (String key : projects.keySet()) {
                System.out.println(key + " -> [" + "Name: " + projects.get(key).get(PROJECT_NAME) + ", Author(s): "
                        + projects.get(key).get(PROJECT_AUTHOR) + "]");
                System.out.println(projects.get(key).get(PROJECT_ENTRY));
            }
            //Remova-os no app final
            zIS = new ZipInputStream(new FileInputStream(mZipFile.getName()));
            System.out.println("Tentar extrair unipack(s) de " + mZipFile.getName());
            writeAllProject(mZipFile, projects, OUTPUT);
        } catch (

        IOException io) {
            for (StackTraceElement s : io.getStackTrace()) {
                System.out.println(s.toString());
            }
        }
    }

    public static String getZipName(ZipFile zipfile) {
        return zipfile.getName().substring(zipfile.getName().lastIndexOf(File.separator) + 1,
                zipfile.getName().lastIndexOf("."));
    }

    public static void writeAllProject(ZipFile zipFile, Map<String, Map<Integer, Object>> projects, String output) {
        for(String name : projects.keySet()){
            writeProject(zipFile, projects.get(name), name, output);
        }
    }

    public static void writeProject(ZipFile zipFile, Map<Integer, Object> project, String name, String output) {
        File write_to = new File(output + File.separator + name);
        String root_entry = (String) project.get(PROJECT_ROOT);
        ArrayList<ZipEntry> project_entrys = (ArrayList<ZipEntry>) project.get(PROJECT_ENTRY);
        if (!write_to.exists()) {
            write_to.mkdirs();
        }
        File file_out;
        File file_out_parent;
        byte[] bytes = new byte[1024];
        int len;
        System.out.println("Root: " + root_entry);
        for (ZipEntry entry : project_entrys) {
            System.out.println("Tentando extrair: " + entry.getName());
            file_out = new File(write_to, entry.getName().replace(root_entry, ""));
            file_out_parent = file_out.getParentFile();
            if (!file_out_parent.exists()) {
                file_out_parent.mkdirs();
            } else {
                if(file_out.exists()){
                    System.out.println("\"" + file_out.getName() + "\" já existe!");
                }
            }
            try {
                InputStream mIS = zipFile.getInputStream(entry);
                FileOutputStream mFOS = new FileOutputStream(file_out);
                while ((len = mIS.read(bytes)) >= 0) {
                    mFOS.write(bytes, 0, len);
                }
                mFOS.close();
                mIS.close();
                System.out.println(entry.getName() + ": Arquivo criado");
            } catch (FileNotFoundException fnfe) {
            } catch (IOException io) {
            }
        }
        //Extração concluida
        zIS = null;
    }

}
