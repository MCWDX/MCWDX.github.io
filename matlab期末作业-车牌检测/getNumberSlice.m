function [charaImg,charaNum] = getNumberSlice(I)
%GETNUMBERSLICE 此处显示有关此函数的摘要
%   此处显示详细说明
    [x,y] = size(I);
    %设置防断阈值, 防止例如J U等字母可能中间断开的情况
    if(y < 600)
        allowance = fix(y/45);
    else
        allowance = fix(y/85);
    end
    whiteColCount = sum(I,1);
    threshold = fix(x/13);
    dividePoint = zeros(20,2);
    PointCount = 0;
    started = false;
    for i = 1:y
        if(whiteColCount(i) >= threshold)
            if(~started)
                PointCount = PointCount+1;
                started = true;
                dividePoint(PointCount,1) = i;
            end
            if(started && i < y && whiteColCount(i+1) < threshold)
                if(i+allowance <= y && whiteColCount(i+allowance) > threshold)
                    i = i + allowance - 1;
                elseif(i+allowance > y && whiteColCount(y) >threshold)
                    i = y-1;
                else
                    started = false;
                    dividePoint(PointCount,2) = i;
                end
            end
        end
        if(started && y == i)
            started = false;
            dividePoint(PointCount,2) = i;
        end
    end

    %先足够宽的或者不宽但是白色够长(用于识别I)的区域
    %作为字符备选区
    outputNum = 0;
    outputs = zeros(size(PointCount,2));
    widths = zeros(size(outputs));
    for i = 1:PointCount
        width = dividePoint(i,2) - dividePoint(i,1);
        whiteHeight = mean(whiteColCount(dividePoint(i,1):dividePoint(i,2)));
        if(width <=fix(y/45) && whiteHeight < fix(0.65*x))
            continue;
        else
            outputNum = outputNum+1;
            outputs(outputNum,1) = dividePoint(i,1);
            outputs(outputNum,2) = dividePoint(i,2);
            widths(outputNum) = width;
        end
    end
    
    %看看有没有几个字粘在一起的分开一下
    widthmean = mean(widths);
    for i = 1:outputNum
        %如果区域宽度在平均长度的1.6倍到2倍就认为是有2个字连一起了
        if(widths(i) >= 1.6*widthmean && widths(i) < 2*widthmean)
            new_outputs = zeros(outputNum+1,2);
            divide = outputs(i,1)+fix(widths(i)/2);
            for j = 1:i-1
                new_outputs(j,1) = outputs(j,1);
                new_outputs(j,2) = outputs(j,2);
            end
            new_outputs(i,1) = outputs(i,1);
            new_outputs(i,2) = divide;
            new_outputs(i+1,1) = divide;
            new_outputs(i+1,2) = outputs(i,2);
            for j = i+1:outputNum
                new_outputs(j+1,1) = outputs(j,1);
                new_outputs(j+1,2) = outputs(j,2);
            end
            outputs = new_outputs;
            outputNum = outputNum+1;
            clear new_outputs;
        %如果区域宽度在平均长度的2倍到2.5倍, 则认为有3个字符连在一起
        elseif(widths(i) >= 2*widthmean && widths(i) < 2.5*widthmean)
            new_outputs = zeros(outputNum+2,2);
            divide1 = outputs(i,1)+fix(widths(i)/3);
            divide2 = outputs(i,1)+fix(widths(i)*2/3);
            for j = 1:i-1
                new_outputs(j,1) = outputs(j,1);
                new_outputs(j,2) = outputs(j,2);
            end
            new_outputs(i,1) = outputs(i,1);
            new_outputs(i,2) = divide1;
            new_outputs(i+1,1) = divide1;
            new_outputs(i+1,2) = divide2;
            new_outputs(i+2,1) = divide2;
            new_outputs(i+2,2) = outputs(i,2);
            for j = i+1:outputNum
                new_outputs(j+2,1) = outputs(j,1);
                new_outputs(j+2,2) = outputs(j,2);
            end
            outputs = new_outputs;
            outputNum = outputNum+2;
            clear new_outputs;
        end
    end
    charaNum = outputNum;
    charaImg = cell(charaNum,1);
    for i = 1:charaNum
        charaImg{i} = I(:,outputs(i,1):outputs(i,2));
    end
end