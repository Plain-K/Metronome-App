#!/usr/bin/env python3
"""
创建节拍器应用图标
"""

from PIL import Image, ImageDraw, ImageFont
import os

# 图标尺寸配置
ICON_SIZES = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192
}

def create_metronome_icon(size):
    """创建节拍器图标"""
    # 创建蓝色背景
    img = Image.new('RGBA', (size, size), (30, 136, 229, 255))  # 蓝色背景
    draw = ImageDraw.Draw(img)
    
    # 绘制圆角矩形背景
    corner_radius = size // 8
    draw.rounded_rectangle([0, 0, size, size], radius=corner_radius, fill=(30, 136, 229, 255))
    
    # 绘制五线谱
    line_color = (255, 255, 255, 100)  # 半透明白色
    line_spacing = size // 12
    start_y = size // 3
    for i in range(5):
        y = start_y + i * line_spacing
        draw.line([(size//10, y), (size*9//10, y)], fill=line_color, width=max(1, size//50))
    
    # 绘制音符（简化版）
    note_color = (255, 255, 255, 255)  # 白色
    
    # 音符头部（椭圆）
    note_x = size // 2
    note_y = size // 2 + size // 8
    note_width = size // 6
    note_height = size // 8
    draw.ellipse([note_x - note_width, note_y - note_height, 
                  note_x + note_width, note_y + note_height], 
                 fill=note_color)
    
    # 音符符干
    stem_width = max(2, size // 30)
    stem_height = size // 2
    draw.rectangle([note_x + note_width - stem_width//2, note_y - stem_height,
                    note_x + note_width + stem_width//2, note_y], 
                   fill=note_color)
    
    # 音符符尾（曲线）
    flag_points = [
        (note_x + note_width, note_y - stem_height),
        (note_x + note_width + size//8, note_y - stem_height + size//10),
        (note_x + note_width + size//12, note_y - stem_height + size//6),
        (note_x + note_width, note_y - stem_height + size//8)
    ]
    draw.polygon(flag_points, fill=note_color)
    
    # 绘制 BPM 文字
    try:
        # 尝试使用系统字体
        font_size = size // 4
        font = ImageFont.truetype("arial.ttf", font_size)
    except:
        # 使用默认字体
        font = ImageFont.load_default()
    
    text = "bpm"
    # 获取文字尺寸
    bbox = draw.textbbox((0, 0), text, font=font)
    text_width = bbox[2] - bbox[0]
    text_height = bbox[3] - bbox[1]
    
    text_x = (size - text_width) // 2
    text_y = size // 6
    
    draw.text((text_x, text_y), text, fill=note_color, font=font)
    
    return img

def generate_all_icons():
    """生成所有尺寸的图标"""
    base_dir = "app/src/main/res"
    
    print("开始生成节拍器图标...")
    
    for folder_name, size in ICON_SIZES.items():
        folder_path = os.path.join(base_dir, folder_name)
        
        # 创建目录
        os.makedirs(folder_path, exist_ok=True)
        
        # 生成图标
        icon = create_metronome_icon(size)
        
        # 保存普通图标
        output_path = os.path.join(folder_path, "ic_launcher.png")
        icon.save(output_path, "PNG")
        print(f"✓ 已生成: {output_path} ({size}x{size})")
        
        # 保存圆形图标
        output_path_round = os.path.join(folder_path, "ic_launcher_round.png")
        icon.save(output_path_round, "PNG")
        print(f"✓ 已生成: {output_path_round} ({size}x{size})")
    
    print("\n🎉 所有图标生成完成！")
    print("请重新构建应用以查看新图标。")

if __name__ == "__main__":
    generate_all_icons()
