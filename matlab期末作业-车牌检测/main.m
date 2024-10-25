clear all;
close;
imgNum = 28;
%读入imgNum张图
% file = fopen("result.txt","w+");
for i = 27:imgNum-1
    clear charaImg;
    loadPath = "LicensePlate\";
    if(length(num2str(i+1)) < 3)
        for j = 1:3-length(num2str(i+1))
            loadPath = loadPath+"0";
        end
    end
    loadPath = loadPath + num2str(i+1);
    %支持jpg,jpeg兩种后缀的图片读入
    try
        img = imread(loadPath+".jpg","jpg");
    catch ME
        try
            img = imread(loadPath+".jpeg");        
        catch ME
            try
                img = imread(loadPath,"png");
            catch ME
                fprintf("Img type error or no such Img\n");
                continue;
            end
        end
    end
    %使用基于车牌背景色为蓝色的hsv模型识别车牌位置重新
    img = hsvLocate(img);
    %矫正图片角度, 使得车牌水平方向排列
    img = rotateLicense(img);
    %使用白色的hsv值将图片二值化
    img = bwByHsv(img);
    %开始分割字符之前再进行一次行定位, 尽可能减少车牌边框出现的情况
    img = bwSecondLocate(img);    
    %开始从左往右扫描字符存储在charaImg中, 以细胞数组的形式存储
    [charaImg,charaNum] = getNumberSlice(img);
    %重新再分割一次车牌号码图片
    charaImg = charaResize(charaImg,charaNum);
    
    str = chara2str(charaImg,charaNum);
    disp((i+1)+":"+str);
%     fprintf(file,"%s\n",str);
end
% fclose(file);