import math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

def create_app_icon(output_path="release/app_icon_512.png", size=512):
    # 4x Supersampling for razor-sharp antialiasing
    scale = 4
    canvas_size = size * scale  # 2048 x 2048
    img = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # 1. Base Squircle / Rounded Background (Modern App Store standard: radius ~ 22%)
    corner_radius = int(canvas_size * 0.22)
    
    # Deep Automotive OLED Background (#0A0D12 -> #121820)
    bg_base = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    bg_draw = ImageDraw.Draw(bg_base)
    bg_draw.rounded_rectangle(
        [(0, 0), (canvas_size, canvas_size)],
        radius=corner_radius,
        fill=(10, 13, 18, 255)
    )

    # Emerald Ambient Glow in the center
    center_glow = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    cg_draw = ImageDraw.Draw(center_glow)
    cx, cy = canvas_size // 2, canvas_size // 2
    for r in range(int(canvas_size * 0.52), 0, -12):
        alpha = int(45 * (1 - r / (canvas_size * 0.52)))
        cg_draw.ellipse(
            [(cx - r, cy - r), (cx + r, cy + r)],
            fill=(16, 185, 129, alpha)
        )
    center_glow = center_glow.filter(ImageFilter.GaussianBlur(radius=70))
    
    img.alpha_composite(bg_base)
    img.alpha_composite(center_glow)
    draw = ImageDraw.Draw(img)

    # 2. Subtle Tech Grid in background
    grid_col = (30, 42, 58, 120)
    for y_rel in [0.22, 0.5, 0.78]:
        y = int(canvas_size * y_rel)
        draw.line([(int(canvas_size * 0.10), y), (int(canvas_size * 0.90), y)], fill=grid_col, width=2 * scale)
    for x_rel in [0.22, 0.5, 0.78]:
        x = int(canvas_size * x_rel)
        draw.line([(x, int(canvas_size * 0.10)), (x, int(canvas_size * 0.90))], fill=grid_col, width=2 * scale)

    # 3. Outer Border Stroke for App Icon
    draw.rounded_rectangle(
        [(scale * 2, scale * 2), (canvas_size - scale * 2, canvas_size - scale * 2)],
        radius=corner_radius,
        outline=(255, 255, 255, 40),
        width=3 * scale
    )

    # 4. Neon Camera Scanner Brackets (Viewfinder)
    bracket_color = (16, 185, 129, 255)  # Emerald #10B981
    glow_color = (16, 185, 129, 90)
    bw = int(12 * scale)
    bl = int(150 * scale)
    
    bx1 = int(canvas_size * 0.08)
    by1 = int(canvas_size * 0.22)
    bx2 = canvas_size - bx1
    by2 = canvas_size - by1

    glow_layer = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    g_draw = ImageDraw.Draw(glow_layer)

    def draw_viewfinder(d, col, w):
        # Top-Left
        d.line([(bx1, by1 + bl), (bx1, by1)], fill=col, width=w)
        d.line([(bx1, by1), (bx1 + bl, by1)], fill=col, width=w)
        # Top-Right
        d.line([(bx2 - bl, by1), (bx2, by1)], fill=col, width=w)
        d.line([(bx2, by1), (bx2, by1 + bl)], fill=col, width=w)
        # Bottom-Left
        d.line([(bx1, by2 - bl), (bx1, by2)], fill=col, width=w)
        d.line([(bx1, by2), (bx1 + bl, by2)], fill=col, width=w)
        # Bottom-Right
        d.line([(bx2 - bl, by2), (bx2, by2)], fill=col, width=w)
        d.line([(bx2, by2), (bx2, by2 - bl)], fill=col, width=w)

    draw_viewfinder(g_draw, glow_color, bw + 16 * scale)
    glow_layer = glow_layer.filter(ImageFilter.GaussianBlur(radius=14 * scale))
    img.alpha_composite(glow_layer)

    draw = ImageDraw.Draw(img)
    draw_viewfinder(draw, bracket_color, bw)

    # 5. Center Uzbekistan License Plate
    plate_w = int(canvas_size * 0.84)
    plate_h = int(plate_w / 3.0)  # Height 570px at 4x
    px1 = (canvas_size - plate_w) // 2
    py1 = (canvas_size - plate_h) // 2
    px2 = px1 + plate_w
    py2 = py1 + plate_h
    plate_radius = int(24 * scale)

    # Shadow under the plate
    shadow_layer = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow_layer)
    s_draw.rounded_rectangle(
        [(px1, py1 + 16 * scale), (px2, py2 + 28 * scale)],
        radius=plate_radius,
        fill=(0, 0, 0, 220)
    )
    shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(radius=22 * scale))
    img.alpha_composite(shadow_layer)

    draw = ImageDraw.Draw(img)

    # Plate Outer Rim (Dark Charcoal #101216)
    draw.rounded_rectangle(
        [(px1, py1), (px2, py2)],
        radius=plate_radius,
        fill=(14, 17, 22, 255),
        outline=(55, 68, 85, 255),
        width=int(4 * scale)
    )

    # Plate White Body
    rim = int(9 * scale)
    draw.rounded_rectangle(
        [(px1 + rim, py1 + rim), (px2 - rim, py2 - rim)],
        radius=plate_radius - rim // 2,
        fill=(255, 255, 255, 255)
    )

    # Section calculations
    inner_left = px1 + rim
    inner_right = px2 - rim
    inner_top = py1 + rim
    inner_bot = py2 - rim
    inner_w = inner_right - inner_left
    inner_h = inner_bot - inner_top

    # Proportions: Region: 17%, Center: 66%, Flag: 17%
    reg_w = int(inner_w * 0.17)
    flag_box_w = int(inner_w * 0.17)
    
    div1_x = inner_left + reg_w
    div2_x = inner_right - flag_box_w
    div_stroke = int(4 * scale)

    # Divider lines
    draw.line([(div1_x, inner_top), (div1_x, inner_bot)], fill=(15, 20, 26, 255), width=div_stroke)
    draw.line([(div2_x, inner_top), (div2_x, inner_bot)], fill=(15, 20, 26, 255), width=div_stroke)

    # 6. Fonts with exact sizing
    try:
        font_reg = ImageFont.truetype("arialbd.ttf", int(48 * scale))
        font_main = ImageFont.truetype("arialbd.ttf", int(58 * scale))
        font_uz = ImageFont.truetype("arialbd.ttf", int(22 * scale))
    except:
        font_reg = ImageFont.load_default()
        font_main = ImageFont.load_default()
        font_uz = ImageFont.load_default()

    mid_y = (inner_top + inner_bot) // 2

    # Draw "01" (Left section)
    r_cx = (inner_left + div1_x) // 2
    draw.text((r_cx, mid_y), "01", fill=(12, 16, 22, 255), font=font_reg, anchor="mm")

    # Draw "777 AAA" (Center section)
    c_cx = (div1_x + div2_x) // 2
    # Measure text to center perfectly
    t_bbox = draw.textbbox((0, 0), "777  AAA", font=font_main)
    draw.text((c_cx, mid_y), "777  AAA", fill=(12, 16, 22, 255), font=font_main, anchor="mm")

    # 7. Right section: Flag & "UZ"
    f_cx = (div2_x + inner_right) // 2
    flag_w = int(flag_box_w * 0.72)
    flag_h = int(flag_w * 0.48)
    
    fx1 = f_cx - flag_w // 2
    fy1 = inner_top + int(inner_h * 0.16)
    fx2 = fx1 + flag_w
    fy2 = fy1 + flag_h

    f_stripe_h = flag_h / 3
    red_thick = max(int(1.6 * scale), 2)

    # Blue top #0099B5
    draw.rectangle([(fx1, fy1), (fx2, fy1 + f_stripe_h)], fill=(0, 153, 181, 255))
    # Red stripe 1
    draw.rectangle([(fx1, fy1 + f_stripe_h - red_thick), (fx2, fy1 + f_stripe_h)], fill=(206, 17, 38, 255))
    # White middle
    draw.rectangle([(fx1, fy1 + f_stripe_h), (fx2, fy1 + 2 * f_stripe_h)], fill=(255, 255, 255, 255))
    # Red stripe 2
    draw.rectangle([(fx1, fy1 + 2 * f_stripe_h), (fx2, fy1 + 2 * f_stripe_h + red_thick)], fill=(206, 17, 38, 255))
    # Green bottom #1EB53A
    draw.rectangle([(fx1, fy1 + 2 * f_stripe_h), (fx2, fy2)], fill=(30, 181, 58, 255))
    # Flag subtle stroke
    draw.rectangle([(fx1, fy1), (fx2, fy2)], outline=(180, 180, 180, 200), width=int(1.2 * scale))

    # "UZ" text below flag
    uz_y = inner_top + int(inner_h * 0.76)
    draw.text((f_cx, uz_y), "UZ", fill=(12, 16, 22, 255), font=font_uz, anchor="mm")

    # 8. Cyan/Emerald Scanning Beam
    laser_y = inner_top + int(inner_h * 0.48)
    laser_layer = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    l_draw = ImageDraw.Draw(laser_layer)
    
    # Wide soft glow
    l_draw.line([(px1 - 30 * scale, laser_y), (px2 + 30 * scale, laser_y)], fill=(16, 185, 129, 60), width=24 * scale)
    # Intense core beam
    l_draw.line([(px1 - 10 * scale, laser_y), (px2 + 10 * scale, laser_y)], fill=(52, 211, 153, 220), width=6 * scale)
    # Center white shine
    l_draw.line([(px1, laser_y), (px2, laser_y)], fill=(255, 255, 255, 245), width=int(2 * scale))

    laser_layer = laser_layer.filter(ImageFilter.GaussianBlur(radius=5 * scale))
    img.alpha_composite(laser_layer)

    # 9. Downsample with Lanczos filter to 512x512
    final_icon = img.resize((size, size), Image.Resampling.LANCZOS)
    final_icon.save(output_path, "PNG", optimize=True)
    print(f"App Icon generated: {output_path}")

if __name__ == "__main__":
    create_app_icon("release/app_icon_512.png", 512)
    create_app_icon("release/store_assets/app_icon_512.png", 512)
