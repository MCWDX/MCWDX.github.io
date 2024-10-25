clear all;
close;
imgNum = 28;
%����imgNum��ͼ
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
    %֧��jpg,jpeg���ֺ�׺��ͼƬ����
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
    %ʹ�û��ڳ��Ʊ���ɫΪ��ɫ��hsvģ��ʶ����λ������
    img = hsvLocate(img);
    %����ͼƬ�Ƕ�, ʹ�ó���ˮƽ��������
    img = rotateLicense(img);
    %ʹ�ð�ɫ��hsvֵ��ͼƬ��ֵ��
    img = bwByHsv(img);
    %��ʼ�ָ��ַ�֮ǰ�ٽ���һ���ж�λ, �����ܼ��ٳ��Ʊ߿���ֵ����
    img = bwSecondLocate(img);    
    %��ʼ��������ɨ���ַ��洢��charaImg��, ��ϸ���������ʽ�洢
    [charaImg,charaNum] = getNumberSlice(img);
    %�����ٷָ�һ�γ��ƺ���ͼƬ
    charaImg = charaResize(charaImg,charaNum);
    
    str = chara2str(charaImg,charaNum);
    disp((i+1)+":"+str);
%     fprintf(file,"%s\n",str);
end
% fclose(file);