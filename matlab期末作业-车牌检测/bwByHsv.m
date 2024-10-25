function [new_I] = bwByHsv(I)
%BWBYHSV 此处显示有关此函数的摘要
%   此处显示详细说明
%   该函数利用hsv值将车牌转化为二值图
%   车牌上除白色车牌号外的部分都转化为黑点0
%   保留车牌上的白点转为白点1
    [x,y,~] = size(I);
    new_I = rgb2hsv(I);
    bw = false(x,y);
    for i = 1:x
        for j = 1:y
            %根据对数据集图片的测试
            %正常白色大概hsv范围在s <= 0.37, v >= 0.73区间内
            %h的值对白色影响不大不作限制
            %但是因为有某几张图片亮度相对较低(车牌数字范围v的值到0.5左右了)
            %故现用0.55作为下限来来测试
            if(new_I(i,j,2) <= 0.37 && new_I(i,j,3) >=0.55)
                bw(i,j) = true;
            end
        end
    end
    new_I = bw;
    new_I = bwmorph(new_I,"hbreak",Inf);
end