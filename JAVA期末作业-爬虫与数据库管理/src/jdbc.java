import java.sql.*;
import java.util.*;
//@SuppressWarnings("unused")
public class jdbc {
    private String url;
    private String user;
    private String pw;
    private String tableName;
    private int opcode = -1;
    private Scanner input;
    private Connection con=null;
    //初始化的时候直接链接数据库
    public jdbc(){
        url = "";
        input = new Scanner(System.in);
        con = null;
        url = "jdbc:mysql://localhost:3306/java";
        user = "mcwdx";
        pw = "12345678";
        tableName = "PAPER";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            con = DriverManager.getConnection(url, user, pw);
            System.out.printf("Database connected\n");
        }
        catch(SQLException e) {
            System.out.printf("Can not connect to database!\n");
            e.printStackTrace();
        }
    }
    //操作菜单
    public void begin(){
        showExistTable();
        System.out.printf("Input table name that u wanna operate:");
        tableName = input.nextLine();
        CreOrSwiTable();
        while(opcode != 0) {
            System.out.println("Available operation listed below:\n"+
            "1:Create table or switch table\n"+
            "2:Add data\n"+
            "3:Delete data\n"+
            "4:Update data\n"+
            "5:Search data\n"+
            "6:Test Code\n"+
            "8:Drop table\n"+
            "0:exit");
            System.out.printf("Operating table %s, opcode:",tableName);
            opcode = input.nextInt();
            input.nextLine();
            switch (opcode) {
                case 1:
                    showExistTable();
                    System.out.printf("Target table name:");
                    tableName = input.next();
                    CreOrSwiTable();
                    break;
                case 2:
                    add();
                    break;
                case 3:
                    del();
                    break;
                case 4:
                    update();
                    break;
                case 5:
                    search();
                    break;
                //本来6 7是留给导入导出操作的,不过后面放弃了这两个操作
                case 6:
                    test();
                    break;
                case 7:
                    break;
                case 8:
                    drop();
                    break;
                default:
                    try{
                        con.close();
                    } catch(SQLException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            System.gc();
        }
    }

    //获取现在数据库中的表名
    public void showExistTable() {
        try{
            String sqlCode = "SHOW TABLES;";
            PreparedStatement pstmt = con.prepareStatement(sqlCode);
            ResultSet rs = pstmt.executeQuery();
            System.out.printf("Tables within database listed below\n");
            while(rs.next()) {
                System.out.printf("%s\n",rs.getString(1));
            }
            rs.close();
            pstmt.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    //获取列名,并且返回以供后续使用
    private String[] getRowName(){
        try{
            String sqlCode = "SELECT * FROM " + tableName + ";";
            PreparedStatement pstmt = con.prepareStatement(sqlCode);
            ResultSet rs = pstmt.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            String[] columnName = new String[columnCount];
            for(int i = 1;i <= columnCount;i++) {
                columnName[i-1] = rsmd.getColumnLabel(i);
            }
            rs.close();
            pstmt.close();
            return columnName;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    //直接输出列名,无返回
    public void printRowName() {
        String[] rowName = getRowName();
        System.out.printf("\nTable %s has row(s):",tableName);
        for(int i = 0;i < rowName.length;i++) {
            System.out.printf("%s ",rowName[i]);
        }
        System.out.printf("\n\n");
    }

    //检查输入的属性名是否合法
    private boolean isValidRowName(String toCheck) {
        String []rows = getRowName();
        List<String> check = Arrays.asList(rows);
        if(check.contains(toCheck)){
            return true;
        } else {
            return false;
        }
    }
    private boolean isValidRowName(String[] toCheck) {
        String []rows = getRowName();
        List<String> check = Arrays.asList(rows);
        for(int i = 0;i<toCheck.length;i++) {
            if(!check.contains(toCheck[i])) {
                return false;
            }
        }
        return true;
    }

    //返回合法属性名,多个属性名用逗号隔开的格式
    private String checkedProperty(String toCheck) {
        boolean isValid = false;
        while(!isValid && !toCheck.equals("0")) {
            isValid = isValidRowName(toCheck.split(","));
            if(!isValid && !toCheck.equals("0")) {
                System.out.printf("Properties error,input again:");
                toCheck = input.nextLine();
            }
        }
        if(isValid){
            return toCheck;
        } else {
            return null;
        }
    }

    //返回输入的合法属性名数组
    private String[] checkedProperties(String toCheck) {
        boolean isValid = false;
        while(!isValid && !toCheck.equals("0")) {
            isValid = isValidRowName(toCheck.split(","));
            if(!isValid && !toCheck.equals("0")) {
                System.out.printf("Properties error,input again:");
                toCheck = input.nextLine();
            }
        }
        if(isValid) {
            return toCheck.split(",");
        } else {
            return null;
        }
    }

    //该方法用于写入sql语句WHERE + 条件的部分,以及UPDATE PAPER SET 后面要设置的部分
    //写这个方法的原因是我发现 增删查改 中只有增是没有WHERE的部分的,其他的都是可选WHERE,写3次代码太冗余
    //但是这里这个方法我个人觉得其实也挺繁琐的,我图方便把UPDATE PAPER SET后面的部分也写这里了,导致用了两次switch
    private StringBuilder updateSqlCode(StringBuilder sqlCode,String updatePart) {
        String propertyStr="",properties[],proValue[],getProStr = "";
        int i = 0;
        switch (updatePart) {
            case "locate":
                sqlCode.append(" WHERE ");
                getProStr = "for locating data";
                break;
            case "update":
                getProStr = "to update";
                break;
            default:
                System.out.printf("Error");
                return null;
        }
        printRowName();
        System.out.printf("Input properties %s,spilt by \",\"\nLike: Author,Abstract\nOr input 0 to exit\nYour input:",getProStr);
        propertyStr = input.nextLine();
        properties = checkedProperties(propertyStr);
        if(properties == null) {
            return null;
        }
        //以上已经获得所需属性名
        //下面要获取属性值
        for(i = 0;i < properties.length;i++) {
            sqlCode.append(properties[i]);
            switch (updatePart) {
                case "locate":
                    //先获取定位数据的操作符,例如 < != = in(AAA,BBB...) BETWEEN a AND b 之类的
                    System.out.printf("Input search operator for searching %s(>,!=,=,in,BETWEEN...):",properties[i]);
                    String operator = input.nextLine();
                    //然后根据操作符和获取对应的输入
                    switch (operator) {
                        case ">":
                        case "<":
                        case "<=":
                        case ">=":
                            if(!properties[i].equals("id")) {
                                System.out.printf("Only numeric properties can use operator >,<,>=,<=\n");
                                return null;
                            }
                        case "!=":
                        case "=":
                            System.out.printf("Input %s for locating data,only one accepted:",properties[i]);
                            propertyStr = input.nextLine();
                            if(properties[i].equals("id")){
                                sqlCode.append(operator + propertyStr);
                            } else{
                                sqlCode.append(operator + "'" + propertyStr + "'");
                            }
                            break;
                        case "IN":
                        case "in":
                            sqlCode.append(" in(");
                            System.out.printf("Input %s for locating data,split by \",\":",properties[i]);
                            propertyStr = input.nextLine();
                            proValue = propertyStr.split(",");
                            for(int j = 0;j < proValue.length;j++) {
                                if(!properties[i].equals("id")){
                                    sqlCode.append("'" + proValue[j] + "'");
                                }else {
                                    sqlCode.append(proValue[j]);
                                }
                                sqlCode.append((j < proValue.length-1)?",":")");
                            }
                            break;
                        case "BETWEEN":
                        case "between":
                            if(!properties[i].equals("id")) {
                                System.out.printf("Only numeric properties can use operator BETWEEN...AND...");
                                return null;
                            }
                            int intValue[] = new int[2];
                            System.out.printf("Input range,smaller first:");
                            intValue[0] = input.nextInt();
                            intValue[1] = input.nextInt();
                            input.nextLine();
                            sqlCode.append(" BETWEEN " + intValue[0] + " AND " + intValue[1]);
                            break;
                        default:
                            break;
                    }
                    sqlCode.append((i < properties.length-1)?" AND ":";");
                    break;
                case "update":
                    //update这边的相对好写一点,就获取个合法属性名和对应属性值就好,不用搞花里胡哨的
                    sqlCode.append("=");
                    System.out.printf("Input %s %s:",(updatePart.equals("update"))?"new":"to search",properties[i]);
                    propertyStr = input.nextLine();
                    if(properties[i].equals("id")){
                        sqlCode.append(propertyStr);
                    } else {
                        sqlCode.append("'" + propertyStr + "'");
                    }
                    sqlCode.append((i < properties.length-1)?", ":"");
                    break;
                default:
                    break;
            }            
                
        }
        return sqlCode;
    }

    //表创建或者切换模块,没有该表则创建,有则切换到操作该表
    public void CreOrSwiTable(){
        try {
            DatabaseMetaData dbm = con.getMetaData();
            ResultSet rs = dbm.getTables(null, null, tableName, null);
            if(!rs.next()) {
                String sqlCode = "CREATE TABLE IF NOT EXISTS " + tableName+" (\n"+
                "id int(11) NOT NULL AUTO_INCREMENT,\n"+
                "Title varchar(100) COLLATE utf8_bin NOT NULL,\n"+
                "Author varchar(100) COLLATE utf8_bin NOT NULL,\n"+
                "Sources varchar(100) COLLATE utf8_bin NOT NULL,\n"+
                "PublicationTime varchar(100) COLLATE utf8_bin NOT NULL,\n"+
                "Abstract varchar(10000) COLLATE utf8_bin NOT NULL,\n"+
                "Link varchar(1000) COLLATE utf8_bin NOT NULL,\n"+
                "PRIMARY KEY(id)\n"+
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=1;";
                Statement pstmt = con.prepareStatement(sqlCode);
                pstmt.executeUpdate(sqlCode);
                System.out.printf("Table %s created\n",tableName);
            } else {
                System.out.printf("Switch to table %s \n",tableName);
            }
        } catch(SQLException e) {
            //默认情况下是操作PAPER表
            System.out.printf("Table creation failed\n");
            tableName = "PAPER";
        }
        System.gc();
    }

    //增添数据模块
    private void add() {
        try {
            PreparedStatement pstmt = null;
            int dataNum = 0,i = 1,j = 0,rs = 0,columnNum = 0;
            String sqlCode = "SELECT * FROM " + tableName + ";";
            String addcolumn = "",dataValue = "";
            //获取需要插入的数据条数
            System.out.printf("Input num of data u wanna insert or 0 to exit:");
            dataNum = input.nextInt();
            input.nextLine();

            if(dataNum > 0){
                //获取表中列名
                String[] column = getRowName();
                columnNum = column.length;
                System.out.printf("\nTable %s has row(s):",tableName);
                //输出列名,除id列的列名
                for(i = 0;i < columnNum;i++) {
                    System.out.printf("%s ",column[i]);
                    addcolumn += (i > 0)?column[i]:"";
                    addcolumn += (i > 0 && i < columnNum -1)?",":"";
                }
                System.out.printf("\n\n");
                column = addcolumn.split(",");
                columnNum = column.length;
                //组装sql语句,需要的值是前面输出的列名,插入数据的时候不需要填id
                sqlCode = "INSERT INTO " + tableName +"(" + addcolumn + ")" + " VALUES";
                for(i = 1;i <= dataNum;i++) {
                    sqlCode += "(";
                    for(j = 1;j <= columnNum;j++) {
                        sqlCode +=(j < columnNum)?"?,":"?";
                    }
                    sqlCode += (i < dataNum)?"),":");";
                }
                pstmt = con.prepareStatement(sqlCode);
                //sql语句组装完毕,开始获取值
                for(i = 1,j = 1;i <= dataNum;i++) {
                    for(int k = 0;k < columnNum;k++) {
                        System.out.printf("Input data%d %s:",i,column[k]);
                        dataValue = input.nextLine();
                        pstmt.setString(j, dataValue);
                        j++;
                    }
                }
                //输出是否插入成功
                rs = pstmt.executeUpdate();
                if(rs > 0) {
                    System.out.printf("Data inserted\n");
                } else {
                    System.out.printf("Data insert failed\n");
                }
            }
            pstmt.close();
        } catch (SQLException e) {
            System.out.printf("Data insert failed\n");
            return ;
        }
        System.gc();
    }
    //给爬虫用的插入新数据的方法
    //如果有指定数据表名字tName就把数据输入tName中,没有的默认插入PAPER中
    //如果插入成功就返回个True给SeleniumTest那边getArt自加(getArt是获取论文数)
    public boolean add(String tName,String Title,String Author,String Sources,String PublicationTime,String Abstract,String Link) {
        tableName = tName;
        return add(Title, Author, Sources, PublicationTime, Abstract, Link);
    }
    public boolean add(String Title,String Author,String Sources,String PublicationTime,String Abstract,String Link) {
        try{
            //数据都在爬虫那边传进来了，直接setString完事
            String sqlCode = "INSERT INTO "+tableName+"(Title,Author,Sources,PublicationTime,Abstract,Link) VALUES (?,?,?,?,?,?);";
            PreparedStatement pstmt = con.prepareStatement(sqlCode);
            pstmt.setString(1, Title);
            pstmt.setString(2, Author);
            pstmt.setString(3, Sources);
            pstmt.setString(4, PublicationTime);
            pstmt.setString(5, Abstract);
            pstmt.setString(6, Link);
            int rs = pstmt.executeUpdate();
            if(rs != 0) {
                System.out.printf("Data inserted\n");
                return true;
            } else {
                return false;
            }
        } catch(SQLException e) {
            return false;
        }
    }

    //删除数据模块
    private void del() {
        try {
            StringBuilder sqlCode = new StringBuilder();
            int mode = -1,rs = 0;
            
            //输出支持的删除模式
            System.out.println("Delete mode:\n"+
            "0:exit\n"+
            "1:delete by properties\n"+
            "2:delete whole table");
            
            //获取输入模式
            System.out.printf("Input ur delete mode:");
            mode = input.nextInt();
            input.nextLine();

            //模式值错误重新输入
            while(mode < 0 || mode >2) {
                System.out.printf("Mode error,input again:");
                mode = input.nextInt();
                input.nextLine();
            }
            
            //模式2的时候删表重新建表(等于清空表)
            if(mode == 2) {
                sqlCode.append("TRUNCATE TABLE "+tableName+";");
                con.prepareStatement(sqlCode.toString()).executeUpdate();
                System.out.printf("Table cleared\n");
            } else if(mode == 1) {
                //模式1的时候按照查找的列值删除行
                PreparedStatement pstmt;
                sqlCode.append("DELETE FROM "+tableName);
                //进方法获取需要删除的数据的属性以及属性值
                sqlCode = updateSqlCode(sqlCode, "locate");
                //rs获取删除了多少条数据并输出数量
                pstmt = con.prepareStatement(sqlCode.toString());
                rs = pstmt.executeUpdate();
                System.out.printf("\n%d data deleted\n\n",rs);
                pstmt.close();
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return ;
    }

    //数据修改模块
    private void update() {
        PreparedStatement pstmt = null;
        StringBuilder sqlCode = new StringBuilder();
        int rs = 0;

        //获取待修改行的待修改属性名
        sqlCode.append("UPDATE " + tableName + " SET ");
        sqlCode = updateSqlCode(sqlCode, "update");
        if(sqlCode == null){
            return;
        }
        //询问是否一次性修改所有数据,输入Y则是,N或者其他字符则不是
        System.out.printf("Is change all data?(Y/N):");
        if(!input.nextLine().equals("Y")) {
            sqlCode = updateSqlCode(sqlCode, "locate");
        }

        //执行更新并且返回影响的数据条数
        try {
            pstmt = con.prepareStatement(sqlCode.toString());
            rs = pstmt.executeUpdate();
            System.out.printf("\n%d data updated\n\n",rs);
        } catch(SQLException e) {
            System.out.printf("Data update failed\n");
        }
    }
    
    //搜索模块
    private void search(){
        try {
            StringBuilder sqlCode = new StringBuilder("SELECT ");
            PreparedStatement pstmt = null;
            String searchPro = "",order = "",output = "";
            int i = 0;
            boolean isOrderValid = false;
            String[] orders = null;
            ResultSet rs = null;
            printRowName();
            System.out.printf("Input what to check\n*:check whole database\nid,Author...:check required data\n0:exit\nYour input:");
            searchPro = input.nextLine();
            //检查输入
            if(!searchPro.equals("*")){
                searchPro = checkedProperty(searchPro);
                if(searchPro == null) {
                    return;
                }
            }
            sqlCode.append(searchPro + " FROM " + tableName);
            //是否查询符合条件的字段,只有输入Y的时候才有搜索条件
            System.out.printf("Have search requirement?(Y/N):");
            if(input.nextLine().equals("Y")) {
                sqlCode = updateSqlCode(sqlCode, "locate");
            }
            //查询出的字段是否按照某种顺序排序
            System.out.printf("Have order requirement?(Y/N):");
            if(input.nextLine().equals("Y")) {
                if(sqlCode.charAt(sqlCode.length()-1) == ';'){
                    sqlCode.deleteCharAt(sqlCode.length()-1);
                }
                sqlCode.append(" ORDER BY ");
                System.out.printf("Order sample: id ASC,Author DESC\n");
                System.out.printf("Input as sample shows:");
                order = input.nextLine();
                orders = order.split(",");
                //判断输入的order是否合法
                while(!isOrderValid){
                    for(i = 0;i < orders.length;i++) {
                        String toCheck[] = orders[i].split(" ");
                        if(!isValidRowName(toCheck[0])){
                            break;
                        }
                        else if(!toCheck[1].equals("ASC") && !toCheck[1].equals("DESC") && 
                        !toCheck[1].equals("desc") && !toCheck[1].equals("asc")){
                            break;
                        }
                    }
                    if(i != orders.length) {
                        System.out.printf("Order error,input again:");
                        order = input.nextLine();
                        orders = order.split(",");
                    } else {
                        isOrderValid = true;
                    }
                }
            }
            if(sqlCode.charAt(sqlCode.length()-1)!=';'){
                sqlCode.append(';');
            }
            //传送sql语句,获取搜索结果
            pstmt = con.prepareStatement(sqlCode.toString());
            rs = pstmt.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            String[] column = new String[rsmd.getColumnCount()];
            for(i = 1 ; i <= rsmd.getColumnCount();i++) {
                column[i-1] = rsmd.getColumnLabel(i);
            }
            System.out.printf("Data acquired,listed below:\n");
            System.out.printf("------------------------------------------------------------\n");
            while(rs.next()) {
                output = "";
                i = 0;
                while(i < column.length) {
                    output = output + column[i] + ":" + rs.getString(column[i]) + "\n";
                    i++;
                }
                System.out.printf("%s",output);
                System.out.printf("------------------------------------------------------------\n");
            }
            rs.close();
            pstmt.close();
        }catch(SQLException e) {
            e.printStackTrace();
        }
    }

    //删除当前操作表
    private void drop(){
        try{
            String sqlCode = "DROP TABLE "+tableName+";";
            PreparedStatement pstmt = con.prepareStatement(sqlCode);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //删除之后立即要求一个新的表名用于操作
        //也就是说可以先drop再输入同一个表名实现删除的mode 1功能
        showExistTable();
        System.out.printf("Input table name that u wanna operate:");
        tableName = input.nextLine();
        CreOrSwiTable();
    }

    private void test() {
        //测试功能,留着需要测试和的时候修改语句用
        String sqlCode = "SHOW CREATE TABLE " + tableName;
        try{
            PreparedStatement pstmt = con.prepareStatement(sqlCode);
            pstmt.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}
