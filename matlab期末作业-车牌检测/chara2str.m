function [str] = chara2str(charaImg,charaNum)
%CHARA2STR 此处显示有关此函数的摘要
%   此处显示详细说明
%   该函数用于识别图片中的字符并输出字符串
    %字符库中从0到9,其中7有两张图,A到Z除去I, O外有34个字符,
    %后面有16个汉字字符(没有把所有车牌能用的中文字放上来),
    %一共51张模板图片(10+1 + 34 + 16 = 51)
    characters = char(['0':'9' '七' 'A':'H' 'J':'N' 'P':'Z' '贵桂京鲁陕苏渝豫粤赣湘冀黑川琼辽']);
    %将所有字符模板图片读入matlab
    bw = false(110,55,51);
    str = "";
    for i = 1:51
        basePic = imread("characters\"+characters(i)+".bmp");
        bw(:,:,i) = ~im2bw(imresize(basePic,[110,55]),graythresh(basePic));
    end
    %通过模板识别字符
    for i = 1:charaNum
        chara = charaImg{i};
        maxFit = 0;
        maxNum = 0;
        if(i == 1)%车牌第一位是汉字
            firstChara = 36;
            lastChara = 51;
        elseif(i==2)%车牌第二个字符只会是字母
            firstChara = 12;
            lastChara = 35;
        else%车牌其他位可以是数字或者字母
            firstChara = 1;
            lastChara = 35;
        end
        %第一种识别方式:逐个像素对比看相似度
        for j = firstChara:lastChara
            fit = 0;
            for row = 1:110
                for col = 1:55
                    if(chara(row,col) == bw(row,col,j))
                        fit = fit+1;
                    end
                end
            end
            if(fit > maxFit)
                maxNum = j;
                maxFit = fit;
            end
        end
        toAdd = characters(maxNum);
        if(toAdd == '七')
            toAdd = '7';
        end
        str = str + toAdd;
    end
end