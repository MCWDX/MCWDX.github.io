function [charaImg] = charaResize(charaImg,charaNum)
%CHARARESIZE 此处显示有关此函数的摘要
%   此处显示详细说明
    figure;
    for i = 1:charaNum
        chara = charaImg{i};
        [x,y] = size(chara);
        if(i >= 2)
            chara = bwareaopen(chara,fix(x*y*0.05));
        end
        if(y <= 20)
            rThreshold = 1;
        elseif(y <= 120)
            rThreshold = fix(y/12);
        else
            rThreshold = fix(y/20);
        end
        if(x <= 20)
            cThreshold = 1;
        elseif(x<=120)
            cThreshold = fix(x/12);
        else
            cThreshold = fix(x/20);%一列白点占10%才能算入范围
        end
        if(cThreshold == 0)
            cThreshold = 1;
        end
        rC = sum(chara,2);
        cC = sum(chara);
        rArea = zeros(5,2);
        cArea = zeros(5,2);
        rAreaC = 0;
        cAreaC = 0;
        rAreaHeight = zeros(5,1);
        cAreaWidth = zeros(5,1);
        flag = false;
        %先行后列的获取方式
        for j = 1:x
            if(rC(j) >= rThreshold && ~flag && j < x)
                flag = true;
                rAreaC = rAreaC+1;
                rArea(rAreaC,1) = j;
            elseif(flag && (j < x && (rC(j+1) < rThreshold) || x == j))
                flag = false;
                rArea(rAreaC,2) = j;
                rAreaHeight(rAreaC) = rArea(rAreaC,2) - rArea(rAreaC,1);
            end
        end
        if(i==1)
            %如果在修正汉字
            top = rArea(1,1);
            bottom = rArea(rAreaC,2);
        else
            %如果在修正字母和数字的边框,
            %这些字符一定是粘在一起的, 
            %不至于有几个单独的区域,
            %那就取白点最多的区域来.
            [~,maxAreaC] = max(rAreaHeight);
            top = rArea(maxAreaC,1);
            bottom = rArea(maxAreaC,2);
        end
        chara1 = chara(top:bottom,:);
        flag = false;
        for j = 1:y
            if(cC(j) >= cThreshold && ~flag && j < y)
                flag = true;
                cAreaC = cAreaC+1;
                cArea(cAreaC,1) = j;
            elseif(flag && (j < y && (cC(j+1) < cThreshold) || y == j))
                flag = false;
                cArea(cAreaC,2) = j;
                cAreaWidth(cAreaC) = cArea(cAreaC,2) - cArea(cAreaC,1);
            end
        end
        if(i==1)
            %如果在修正汉字
            left = -1;
            right = -1;
            for j = 1:cAreaC
                if(cAreaWidth(j) > fix(y/10) && left < 0)
                    left = cArea(j,1);
                end
                if(left > 0 && ((j < cAreaC && cAreaWidth(j+1) <= fix(y/10)) || j == cAreaC))
                    right = cArea(j,2);
                end
            end
        else
            %如果在修正字母和数字的边框,
            %这些字符一定是粘在一起的, 
            %不至于有几个单独的区域,
            %那就取白点最多的区域来.
            [~,maxAreaC] = max(cAreaWidth);
            left = cArea(maxAreaC,1);
            right = cArea(maxAreaC,2);
        end
        chara1 = chara1(:,left:right);
        chara1 = imresize(chara1,[110,55]);
        charaImg{i} = chara1;
        subplot(1,charaNum,i);
        imshow(chara1);
    end
end