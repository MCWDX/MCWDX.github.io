function [new_I] = bwSecondLocate(I)
%BWSECONDLOCATE 此处显示有关此函数的摘要
%   此处显示详细说明
    [x,~] = size(I);
    whiteRowCount = sum(I,2);
    %前面已经进行过车牌粗劣定位,  
    %车牌现在基本上都在图片正中, 
    %应该可以直接使用x/2定位
    top = ceil(x/2);
    bottom = top;
    threshold = mean(whiteRowCount)/1.8;
    while(whiteRowCount(top) > threshold && top > 1)
        top = top-1;
    end
    if(top-5 < 1)
        top = 1;
    else
        top = top-5;
    end
    while(whiteRowCount(bottom) > threshold && bottom < x)
        bottom = bottom+1;
    end
    if(bottom+5 > x)
        bottom = x;
    else
        bottom = bottom+5;
    end
    new_I = I(top:bottom,:);
end