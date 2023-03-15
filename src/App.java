import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class App {
    final static int KEYLED_COUNT = 0;
    final static int SOUNDS_COUNT = 1;
    final static int PROJECT_FILE_COUNT = 2;
    final static int PROJECT_NAME = 3;
    final static int PROJECT_AUTHOR = 4;
    final static int PROJECT_ENTRY = 5;
    final static int PROJECT_ROOT = 6;
    final static int ZIP_DIR = 7;

    final static String PACK1 = "C:\\Mobile\\Unipack\\Sideways.zip";
    final static String PACK2 = "C:\\Mobile\\Unipack\\RISE (ft. The Glitch Mob, Mako, and The Word Alive).zip";
    final static String PACK3 = "C:\\Mobile\\Unipack\\projetos teste.zip";
    final static String OUTPUT = "C:\\Mobile\\Unipack\\outputs_zip";

    public static void main(String[] args) {
        getProjectsFromZip(new String[] { PACK1, PACK2, PACK3 });
    }

    public static void getProjectsFromZip(String[] zip_dirs /* Diretório(s) do(s) Zip(s) */) {
        Map<String, Map<Integer, Object>> projects = new HashMap<>();

        ZipFile mZipFile;
        ZipEntry mZipEntry;
        Enumeration<? extends ZipEntry> enums;

        String zip_file_name;
        String project_name;
        String parent_folder_name;
        String file_name;
        String project;
        String parent_folder;
        String root_project;
        File entry_file;

        // Temporários
        int sound_count;
        int led_count;
        ZipEntry tmp_entry;

        for (String zip : zip_dirs) {
            try {
                mZipFile = new ZipFile(zip);
                enums = mZipFile.entries();
                zip_file_name = getZipName(mZipFile);

                nextElement: while (enums.hasMoreElements()) {
                    mZipEntry = enums.nextElement();
                    entry_file = new File(mZipEntry.getName());
                    file_name = entry_file.getName();
                    parent_folder = entry_file.getParent();
                    /*
                     * Definir as variáveis para facilmente detectar uma Unipack
                     * Verifique se o arquivo tem pasta pai. Se não houver então estamos na pasta raíz.
                     * Caso haja verifique se ele o pai contém o arquivo "info". Se sim, defina as variáveis.
                     * Se não, verifique se o pai tem pai. Se sim, o pai do pai será o nome do projeto
                     * (mesmo que não tenha o "info" pois essa verificação é feita depois).
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
                    if (mZipEntry.isDirectory()) {
                        /*
                         * Não tenho interesse se for uma pasta. Caso seja um arquivo
                         */
                        continue nextElement;
                    }
                    /*
                     * Verifique se isto é um projeto Unipad
                     */
                    tmp_entry = (mZipFile.getEntry(root_project + "Info") != null)
                            ? mZipFile.getEntry(root_project + "Info")
                            : mZipFile.getEntry(root_project + "info");
                    if (tmp_entry != null) {
                        if (!root_project.equals("")
                                && !new File(root_project).getName().equalsIgnoreCase(project_name)) {
                            project_name = new File(root_project).getName();
                        }
                        if (!projects.containsKey(project_name)) {
                            /*
                             * Arquivo é criada um mapa para o projeto contendo:
                             * Número de leds, número de samples, nome do projeto (Info), nome do(s) autor(es)(Info)
                             * localização do arquivo zip, o diretório root dentro do zip do projeto, quantidade de
                             * arquivos no diretório do projeto e a lista dos diretórios a partir do diretório do projeto 
                             */
                            ArrayList<ZipEntry> entry_list = new ArrayList<>();
                            Map<Integer, Object> project_properties = new HashMap<>();
                            InputStream mIS = mZipFile.getInputStream(tmp_entry);
                            InputStreamReader mISR = new InputStreamReader(mIS, "UTF-8");
                            Scanner mS = new Scanner(mISR);
                            String title = "";
                            String producerName = "";
                            String line;
                            while (mS.hasNextLine()) {
                                /*
                                 * Processo para ler o arquivo "info" deretamente do zip.
                                 * Útil para apresentar o nome e autor do projeto
                                 */
                                line = mS.nextLine().trim();
                                if (line.indexOf("title") == 0) {
                                    title = line.substring(line.indexOf("=") + 1).trim();
                                } else if (line.indexOf("producerName") == 0) {
                                    producerName = line.substring(line.indexOf("=") + 1).trim();
                                }
                                if (!title.equals("") && !producerName.equals(""))
                                    break;
                            }
                            mS.close();
                            mISR.close();
                            mIS.close();
                            project_properties.put(PROJECT_NAME, title);
                            project_properties.put(PROJECT_AUTHOR, producerName);
                            project_properties.put(PROJECT_ROOT, root_project);
                            project_properties.put(ZIP_DIR, mZipFile.getName());
                            project_properties.put(KEYLED_COUNT, 0);
                            project_properties.put(SOUNDS_COUNT, 0);
                            project_properties.put(PROJECT_FILE_COUNT, 0);
                            project_properties.put(PROJECT_ENTRY, entry_list);
                            projects.put(project_name, project_properties);
                        }
                        /*
                         * Adicione os arquivos ao mapa pois este é uma Unipack
                         * Verifique se é arquivo de led/som e adicione +1 se for verdade
                         */
                        ((ArrayList<ZipEntry>) projects.get(project_name).get(PROJECT_ENTRY)).add(mZipEntry);
                        /*
                         * Verifique se o arquivo atual é uma sample ou uma led.
                         * Se sim, acrecente +1 na contagem
                         * Verificação da led: Estrutura padrão é "0 0 0 0" e verifique
                         * se a pasta pai começa com "keyled".
                         * Verificação da sample: Extenção mais comum em Unipack são ".wav" e ".mp3"
                         * e verifique se a pasta pais se chama "sounds".
                         */
                        if (file_name.matches("\\d\\s\\d\\s\\d\\s\\d.*")
                                && parent_folder_name.toLowerCase().indexOf("keyled") == 0) {
                            led_count = (int) projects.get(project_name).get(KEYLED_COUNT);
                            led_count++;
                            projects.get(project_name).put(KEYLED_COUNT, (int) led_count);
                        } else if (file_name.matches("(.*\\.wav|.*\\.mp3)")
                                && parent_folder_name.equalsIgnoreCase("sounds")) {
                            sound_count = (int) projects.get(project_name).get(SOUNDS_COUNT);
                            sound_count++;
                            projects.get(project_name).put(SOUNDS_COUNT, (int) sound_count);
                        }
                    }
                }
            } catch (IOException io) {
                // Arquivo zip não existe
                for (StackTraceElement s : io.getStackTrace()) {
                    System.out.println(s.toString());
                }
            }
        }
        // Remova-os no app final v

        System.out.println("Project count: " + projects.keySet().size() + "\n");
        for (String key : projects.keySet()) {
            System.out.println("Name: " + projects.get(key).get(PROJECT_NAME) + "\nAuthor(s): "
                    + projects.get(key).get(PROJECT_AUTHOR) + "\n" +
                    "Project files: " + ((ArrayList<ZipEntry>) projects.get(key).get(PROJECT_ENTRY)).size() + "\n"
                    + "Led count: " + projects.get(key).get(KEYLED_COUNT) + "\n" +
                    "Sounds count: " + projects.get(key).get(SOUNDS_COUNT) + "\n ---------------------------");
        }
        writeAllProject(projects, OUTPUT);

        // Remova-os no app final ^

    }

    public static String getZipName(ZipFile zipfile) {
        return zipfile.getName().substring(zipfile.getName().lastIndexOf(File.separator) + 1,
                zipfile.getName().lastIndexOf("."));
    }

    public static void writeAllProject(Map<String, Map<Integer, Object>> projects, String output) {
        /*
         * A verificação de tamanho do mapa que contém os projetos é quase inútil mas optei por deixar.
         * Obtenha cada mapa e chame writeProject() para fazer o resto.
         * O nome da chave será o nome da pasta raiz do projeto.
         */
        if (projects.size() > 0) {
            for (String name : projects.keySet()) {
                try {
                    writeProject(new ZipFile((String) projects.get(name).get(ZIP_DIR)), projects.get(name), name,
                            output);
                } catch (IOException io) {
                    // Arquivo zip não existe ou foi movido após a leitura
                }
            }
        }
    }

    public static void writeProject(ZipFile zipFile, Map<Integer, Object> project, String name, String output) {
        /*
         * Isso irá "extrair" os arquivos do zipFile, obtendo os diretório que está em project com get(PROJECT_ENTRY).
         * get(PROJECT_ENTRY) retorna uma lista dos diretório. Prefiro assim por ser mais fácil e também para facilitar
         * a extração de múltiplos projetos sem precisar fazer a verificação novamente, buscar os arquivos certo, trabalho
         * que getProjectsFromZip() já faz.
         * root_entry é o diretório raíz de onde está o arquivo "info", que indica que o projeto começa lá
         * útil para quando o projeto está muito mais longe da ráiz, que é "" no zip, como, por exemplo, "pasta1/pasta2/pasta3/projeto"
         * o nome da pasta "root_entry" é usado para criar a pasta no diretório de extração. Neste caso terá de ser "<EXTRAC_TO_DIR>/projeto/"
         * Após isso, esse detório é definido como a raíz para extração do demais arquivos contidos em root_entry.
         * O nome exato do arquivo será obtido pegando a entry e subtituindo a raíz no zip pela raíz da extração, ou seja:
         * entry = "pasta1/pasta2/pasta3/projeto/info"
         * root_entry = "pasta1/pasta2/pasta3/projeto" (Raíz do projeto no zip)
         * write_to = "<EXTRAC_TO_DIR>/" (Raiz de extração)
         * Substituindo:
         * file_out = em entry, substitua root_entry por write, resultando em "<EXTRAC_TO_DIR>/projeto/info" 
         * Verifique se a pasta pai existe e se não existe, então, crie-o (como "keyled" e "sounds").
         */
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
        for (ZipEntry entry : project_entrys) {
            file_out = new File(write_to, entry.getName().replace(root_entry, ""));
            file_out_parent = file_out.getParentFile();
            if (!file_out_parent.exists()) {
                file_out_parent.mkdirs();
            } else {
                if (file_out.exists()) {
                    // Coloque aqui a operação de substituir, pular ou cancelar
                }
            }
            try {
                /*
                 * Preparação para extração e extração
                 */
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
        // Extração concluida
    }
}
