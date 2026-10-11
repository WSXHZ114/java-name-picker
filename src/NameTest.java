import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Random;

public class NameTest {
    //最终版本
    public static void main(String[] args) throws IOException {
        
        //创建一个集合。后续存放名字信息
        ArrayList<String> namesList = new ArrayList<>();
        //调用方法，获取工作目录。后续创建及检测文件时使用
        File baseDir = getBaseDir();
        BufferedReader nameReader = null;
        //根据工作目录设置文件路径
        File namesFile = new File(baseDir, "Name.txt");
        File nameCountFile = new File(baseDir, "Name-Count.txt");
        //名字信息是否加载成功的标签，默认为加载失败
        boolean namesFileLoaded = false;
        //检测名字文件是否存在
        //如果存在就创建一个Reader读取对象并把名字信息设置为加载成功，后续用来读取文件
        //不存在则创建新的文件
        if (namesFile.exists()) {
            //显式指定 UTF-8，避免在中文 Windows 上被按 GBK 读取导致姓名乱码
            nameReader = Files.newBufferedReader(namesFile.toPath(), StandardCharsets.UTF_8);
            namesFileLoaded = true;
        } else {
            namesFile.createNewFile();
        }
        String nameLine;
        //如果名字信息加载成功，就获取所有的信息并筛选，再根据规则添加至内存中
        //如果名字信息加载失败，则提示"已创建Name文件，请补充内容"
        //                     （在24行时已经创建成功，但没有提示）
        if (namesFileLoaded) {
            while ((nameLine = nameReader.readLine()) != null) {
                nameLine = stripBom(nameLine);
                String[] split = nameLine.split("-");
                //按'-'切割字符串，手动清洗数据添加至集合，防止出现问题
                if (split.length == 3) {
                    String part1 = split[0].trim();
                    String part2 = split[1].trim();
                    String part3 = split[2].trim();

                    if (!part1.isEmpty() && !part2.isEmpty() && !part3.isEmpty()) {
                        namesList.add(part1 + "-" + part2 + "-" + part3);
                    }
                }
            }
        } else {
            System.out.println("已创建Name文件，请补充内容");
        }
        //如果名字信息加载成功，则关流
        if (namesFileLoaded) {
            nameReader.close();
        }
        //检测计数器文件是否存在
        //如果存在就开始运行点名方法
        //不存在则创建新的文件并初始化，随后退出程序
        if (nameCountFile.exists()) {
            start(namesList, baseDir, nameCountFile);
        } else {
            nameCountFile.createNewFile();
            Files.write(nameCountFile.toPath(), "0".getBytes(StandardCharsets.UTF_8));
            System.out.println("已创建Name-Count并初始化");
            System.exit(0);
        }
    }

    public static void start(ArrayList<String> namesList, File baseDir, File nameCountFile) throws IOException {
        //检测加到内存中的名字清单是否为空
        //如果是，那么就返回
        if (namesList.isEmpty()) {
            System.out.println("加载Name文件时失败");
            return;
        }
        //根据工作目录设置文件路径
        File cheatConfigFile = new File(baseDir, "Name-Cheating.txt");
        String cheatConfigLine = null;
        //检测作弊文件是否存在
        //如果存在就读取第一行的内容
        if (cheatConfigFile.exists()) {
            BufferedReader cheatConfigReader =
                    Files.newBufferedReader(cheatConfigFile.toPath(), StandardCharsets.UTF_8);
            cheatConfigLine = stripBom(cheatConfigReader.readLine());
            cheatConfigReader.close();
        }

        BufferedReader nameCountReader =
                Files.newBufferedReader(nameCountFile.toPath(), StandardCharsets.UTF_8);
        String countLine = stripBom(nameCountReader.readLine());
        nameCountReader.close();
        //初始化值
        //cheatAtCount：作弊文件内的指定计数
        //currentCount：默认的计数器
        //cheatEnabled：作弊是否开启的标志
        //cheatNameFound：名字是否找到的标志
        int cheatAtCount = 0;
        int currentCount = 0;
        boolean cheatEnabled = false;
        boolean cheatNameFound = false;

        //检测作弊功能是否开启：配置行必须同时包含 "/" 和一个非空的名字前缀
        if (cheatConfigLine != null && cheatConfigLine.contains("/")) {
            String[] split = cheatConfigLine.split("/");
            if (split.length >= 2 && !split[1].trim().isEmpty()) {
                cheatEnabled = true;
            }
        }
        try {
            //计数器为null或为空时
            //调用resetCountFile方法初始化值并退出程序
            if (countLine == null || countLine.isEmpty()) {
                resetCountFile(nameCountFile);
            } else {
                currentCount = Integer.parseInt(countLine);
            }
            //报错时，调用resetCountFile方法初始化值并退出程序
        } catch (NumberFormatException e) {
            resetCountFile(nameCountFile);
        }

        if (cheatEnabled) {
            //按'/'切割字符串
            String[] split = cheatConfigLine.split("/");
            String strCount = split[0].trim();  //为计数
            String strName = split[1].trim();   //为名字
            try {
                //如果为空时赋值-1，执行不到
                cheatAtCount = strCount.isEmpty() ? -1 : Integer.parseInt(strCount);
            } catch (NumberFormatException e) {
                System.out.println("加载Name-Cheating时失败，已初始化默认为null");
                Files.write(cheatConfigFile.toPath(), "null".getBytes(StandardCharsets.UTF_8));
                System.exit(0);
            }
            //+1是为了符合用户使用习惯
            if (currentCount + 1 == cheatAtCount) {
                //创建集合，后续添加合适的名字
                ArrayList<String> matchedNames = new ArrayList<>();
                for (int i = 0; i < namesList.size(); i++) {
                    boolean matched = namesList.get(i).startsWith(strName);
                    if (matched) {
                        cheatNameFound = true;
                        matchedNames.add(namesList.get(i));
                    }
                }
                if (!cheatNameFound) {
                    System.out.println("没有" + "'" + strName + "'");
                    currentCount++;
                } else {
                    //在集合里面随机抽取一个名字
                    //      （同姓时就可以随机抽选）
                    Random rand = new Random();
                    int index = rand.nextInt(matchedNames.size());
                    System.out.println(matchedNames.get(index));
                    currentCount++;
                }
            } else {
                Random rand = new Random();
                int randomIndex = rand.nextInt(namesList.size());
                System.out.println(namesList.get(randomIndex));
                currentCount++;
            }
            Files.write(nameCountFile.toPath(), String.valueOf(currentCount).getBytes(StandardCharsets.UTF_8));
        } else {
            Random rand = new Random();
            int randomIndex = rand.nextInt(namesList.size());
            System.out.println(namesList.get(randomIndex));
            currentCount++;
            Files.write(nameCountFile.toPath(), String.valueOf(currentCount).getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void resetCountFile(File nameCountFile) throws IOException {
        //计数器文件为空或内容损坏时，重置为 0 并退出，避免后续解析继续出错
        System.out.println("加载Name-Count时失败，已初始化默认为0开始");
        Files.write(nameCountFile.toPath(), "0".getBytes(StandardCharsets.UTF_8));
        System.exit(0);
    }

    private static String stripBom(String line) {
        //去掉记事本 / PowerShell 保存 UTF-8 时写在文件开头的 BOM，否则第一个姓名会多出一个不可见字符
        if (line != null && line.startsWith("\uFEFF")) {
            return line.substring(1);
        }
        return line;
    }

    private static File getBaseDir() {
        //获取项目工作目录
        //1.IDEA
        //2.jar包
        try {
            File codeSource = new File(
                    NameTest.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );

            if (codeSource.isFile()) {
                return codeSource.getParentFile();
            }
            return new File(System.getProperty("user.dir"));

        } catch (Exception e) {
            return new File(System.getProperty("user.dir"));
        }
    }
}
