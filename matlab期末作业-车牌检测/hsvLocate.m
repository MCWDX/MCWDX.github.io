function [new_I] = hsvLocate(I)
%hsvLocate 此处显示有关此函数的摘要
%   此处显示详细说明
%   使用hsv值来获取车牌位置
    hsvI = rgb2hsv(I);
    [x,y,~] = size(hsvI);
    ranges = [0.5,0.72,0.35,1,0.35,1,0,0];
    bw = false(x,y);
    for i = 1:x
        for j = 1:y
            if(hsvI(i,j,1) >= ranges(1) && hsvI(i,j,1) <= ranges(2) && ...
               hsvI(i,j,2) >= ranges(3) && hsvI(i,j,2) <= ranges(4) && ...
               hsvI(i,j,3) >= ranges(5) && hsvI(i,j,3) <= ranges(6))
                bw(i,j) = true;
            end
        end
    end
    bw = imerode(bw,strel('rectangle',[6,6]));
    bw = imclose(bw,strel('rectangle',[50,50]));
    bw = bwareaopen(bw,2500);
    left = -1;
    right = -1;
    top = -1;
    bottom = -1;
    colCount = sum(bw,1);
    colAvg = sum(colCount)/sum(colCount~=0);
    rowCount = sum(bw,2);
    rowAvg = sum(rowCount)/sum(rowCount~=0);
    if(rowAvg <= 600)
        ranges(7) = fix(rowAvg*0.5);
    else
        ranges(7) = fix(rowAvg*0.25);
    end
    ranges(8) = fix(colAvg*0.8);
    %对行的搜索改成从中间往上下找车牌区域
    %先确认一下中间那一行是不是正好符合车牌区域的白点阈值
    if(rowCount(ceil(x/2)) >= ranges(7))
        top = ceil(x/2);
        bottom = ceil(x/2);
        while(rowCount(top) >= ranges(7) && top > 1)
            top = top-1;
        end
        while(rowCount(bottom) >= ranges(7) && bottom < x)
            bottom = bottom+1;
        end
    else
        %中间那行不在车牌区域范围内时, 往上下搜索,
        %离中心最近的那行优先认为是车牌区域
        for i = ceil(x/2):-1:1
            if(rowCount(x-i) >= ranges(7))
                top = x-i-1;
                bottom = x-i;
                while(rowCount(bottom) >= ranges(7) && bottom < x)
                    bottom = bottom+1;
                end
                break;
            elseif(rowCount(i) >= ranges(7))
                bottom = i+1;
                top = i;
                while(rowCount(top) >= ranges(7))
                    top = top-1;
                end
                break;
            end
        end
    end
    %根据实验结果选用从两端往中间扫描列
    for i = 1:ceil(y/2)
        if(colCount(i) >= ranges(8))
           if(left < 0)
               left = i;
           end
           if(colCount(i+1) < ranges(8) && right < 0)
               right = i+1;
           end
        end
        if(colCount(y-i) >= ranges(8))
            if(right <0)
                right = y-i;
            end
            if(colCount(y-i-1) < ranges(8)  && left < 0)
                left = y-i-1;
            end
        end
        if(left > 0 && right > 0)
            %防止扫描到左边或者右边的某个非车牌区域后停下
            %样本图片的车牌一般都在偏中间的位置
            newAreaCount = sum(bw(top:bottom,left:right),2);
            fitRow = newAreaCount(newAreaCount > ranges(7));
            if(sum(fitRow)/(bottom - top) < 0.5)
                left = -1;
                right = -1;
            else
                break;
            end
        end
    end
    %适当调整区域以防缺行缺列
    if(top-10 > 0)
        top = top-10;
    else
        top = 1;
    end
    if(bottom+10 < x)
        bottom = bottom+10;
    else
        bottom = x;
    end
    if(left-3 > 0)
        left = left-3;
    else
        left = 1;
    end
    if(right+3 < y)
        right = right+3;
    else
        right = y;
    end
    new_I = I(top:bottom,left:right,:);
end