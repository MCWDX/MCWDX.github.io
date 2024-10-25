function [rot_I] = rotateLicense(I)
%ROTATELICENSE 此处显示有关此函数的摘要
%   此处显示详细说明
%   此函数用于将车牌旋转至水平方向
%   基本上大部分车牌都可以正确旋转
    %图片转灰度图并获取边沿
    rot_I = rgb2gray(I);
    [rot_I,~] = edge(rot_I,'canny');
    %利用radon获取车牌的旋转角度
    theta = 1:180;
    R = radon(rot_I,theta);
    [~,angle] = find(R==max(max(R)));%获取旋转角度
    rot_I = imrotate(I,90-angle,"crop");
end