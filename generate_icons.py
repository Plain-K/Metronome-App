#!/usr/bin/env python3
"""
生成Android应用图标所需的各种尺寸
"""

from PIL import Image
import os
import sys

# 图标尺寸配置
ICON_SIZES = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192
}

def generate_icons(source_image_path):
    """生成所有尺寸的图标"""

    # 检查源文件是否存在
    if not os.path.exists(source_image_path):
        print(f"错误: 找不到源文件 {source_image_path}")
        return False

    # 打开源图像
    try:
        source_img = Image.open(source_image_path)
    except Exception as e:
        print(f"错误: 无法打开图像文件 - {e}")
        return False

    # 确保图像是正方形
    width, height = source_img.size
    if width != height:
        print(f"警告: 图像不是正方形 ({width}x{height})，将裁剪为正方形")
        min_size = min(width, height)
        left = (width - min_size) // 2
        top = (height - min_size) // 2
        right = left + min_size
        bottom = top + min_size
        source_img = source_img.crop((left, top, right, bottom))

    # 转换为RGBA模式（支持透明）
    if source_img.mode != 'RGBA':
        source_img = source_img.convert('RGBA')

    base_dir = "app/src/main/res"

    # 为每个尺寸生成图标
    for folder_name, size in ICON_SIZES.items():
        folder_path = os.path.join(base_dir, folder_name)

        # 创建目录（如果不存在）
        os.makedirs(folder_path, exist_ok=True)

        # 调整图像大小
        resized_img = source_img.resize((size, size), Image.Resampling.LANCZOS)

        # 保存为PNG格式
        output_path = os.path.join(folder_path, "ic_launcher.png")
        resized_img.save(output_path, "PNG")
        print(f"已生成: {output_path} ({size}x{size})")

        # 同时生成圆形图标（如果需要）
        output_path_round = os.path.join(folder_path, "ic_launcher_round.png")
        resized_img.save(output_path_round, "PNG")
        print(f"已生成: {output_path_round} ({size}x{size})")

    print("\n图标生成完成！")
    return True

if __name__ == "__main__":
    # 检查命令行参数
    if len(sys.argv) < 2:
        # 默认查找常见的图标文件名
        possible_names = ["icon.png", "ic_launcher.png", "app_icon.png", "logo.png"]
        found = False

        for name in possible_names:
            if os.path.exists(name):
                print(f"找到图标文件: {name}")
                generate_icons(name)
                found = True
                break

        if not found:
            print("用法: python generate_icons.py <图标文件路径>")
            print("或请将图标文件命名为 icon.png 放在当前目录")
    else:
        generate_icons(sys.argv[1])
