from pathlib import Path
from math import pow
from PIL import Image, ImageDraw, ImageFilter

W, H = 640, 1200
SCALE = 3
OUT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res" / "drawable-nodpi"
OUT.mkdir(parents=True, exist_ok=True)


def rgba(value, alpha=255):
    value = value.lstrip("#")
    return tuple(int(value[i:i + 2], 16) for i in (0, 2, 4)) + (alpha,)


def mix(a, b, amount):
    amount = max(0.0, min(1.0, amount))
    return tuple(round(a[i] + (b[i] - a[i]) * amount) for i in range(4))


def sc_point(point):
    return tuple(round(v * SCALE) for v in point)


def cubic(p0, p1, p2, p3, steps=24):
    points = []
    for index in range(1, steps + 1):
        t = index / steps
        u = 1.0 - t
        x = pow(u, 3) * p0[0] + 3 * pow(u, 2) * t * p1[0] + 3 * u * pow(t, 2) * p2[0] + pow(t, 3) * p3[0]
        y = pow(u, 3) * p0[1] + 3 * pow(u, 2) * t * p1[1] + 3 * u * pow(t, 2) * p2[1] + pow(t, 3) * p3[1]
        points.append((x, y))
    return points


def quad(p0, p1, p2, steps=18):
    points = []
    for index in range(1, steps + 1):
        t = index / steps
        u = 1.0 - t
        points.append((u * u * p0[0] + 2 * u * t * p1[0] + t * t * p2[0],
                       u * u * p0[1] + 2 * u * t * p1[1] + t * t * p2[1]))
    return points


def path_points(commands):
    points = []
    current = None
    start = None
    for command in commands:
        op = command[0]
        if op == "M":
            current = (command[1], command[2])
            start = current
            points.append(current)
        elif op == "L":
            current = (command[1], command[2])
            points.append(current)
        elif op == "C":
            end = (command[5], command[6])
            points.extend(cubic(current, (command[1], command[2]), (command[3], command[4]), end))
            current = end
        elif op == "Q":
            end = (command[3], command[4])
            points.extend(quad(current, (command[1], command[2]), end))
            current = end
        elif op == "Z" and start is not None:
            points.append(start)
            current = start
    return points


def polygon(draw, points, fill, outline=None, width=4):
    scaled = [sc_point(point) for point in points]
    draw.polygon(scaled, fill=fill)
    if outline and width > 0:
        draw.line(scaled + [scaled[0]], fill=outline, width=round(width * SCALE), joint="curve")


def shape(draw, commands, fill, outline=None, width=4):
    polygon(draw, path_points(commands), fill, outline, width)


def line(draw, points, fill, width=4, joint="curve"):
    draw.line([sc_point(p) for p in points], fill=fill, width=round(width * SCALE), joint=joint)


def ellipse(draw, bounds, fill, outline=None, width=1):
    scaled = tuple(round(v * SCALE) for v in bounds)
    draw.ellipse(scaled, fill=fill, outline=outline, width=max(1, round(width * SCALE)))


def rounded_rectangle(draw, bounds, radius, fill, outline=None, width=1):
    scaled = tuple(round(v * SCALE) for v in bounds)
    draw.rounded_rectangle(scaled, radius=round(radius * SCALE), fill=fill, outline=outline,
                           width=max(1, round(width * SCALE)))


CHARACTERS = [
    {
        "file": "character_city_explorer.png",
        "variant": 0,
        "skin": "#D6A77F",
        "hair": "#18212D",
        "hair_hi": "#34445A",
        "jacket": "#31465E",
        "jacket_hi": "#45627D",
        "jacket_shadow": "#223247",
        "shirt": "#E8F0EE",
        "pants": "#121A2A",
        "accent": "#FF8138",
        "shoe": "#1A2631",
    },
    {
        "file": "character_tech_scout.png",
        "variant": 1,
        "skin": "#D9A87A",
        "hair": "#1B2730",
        "hair_hi": "#2F5962",
        "jacket": "#284D56",
        "jacket_hi": "#39737A",
        "jacket_shadow": "#183841",
        "shirt": "#CDE7E3",
        "pants": "#16242B",
        "accent": "#55D7D1",
        "shoe": "#172229",
    },
    {
        "file": "character_museum_researcher.png",
        "variant": 2,
        "skin": "#D3A173",
        "hair": "#5A3825",
        "hair_hi": "#84553A",
        "jacket": "#506950",
        "jacket_hi": "#6F8665",
        "jacket_shadow": "#344B3A",
        "shirt": "#E7D7B8",
        "pants": "#323833",
        "accent": "#C9A66B",
        "shoe": "#3A2A22",
    },
    {
        "file": "character_street_player.png",
        "variant": 3,
        "skin": "#D7A376",
        "hair": "#2B2038",
        "hair_hi": "#62407A",
        "jacket": "#583287",
        "jacket_hi": "#784CAA",
        "jacket_shadow": "#38214F",
        "shirt": "#1D2029",
        "pants": "#252837",
        "accent": "#F0D73D",
        "shoe": "#181A23",
    },
    {
        "file": "character_fantasy_guardian.png",
        "variant": 4,
        "skin": "#DDB083",
        "hair": "#DDD5C4",
        "hair_hi": "#F5F0E6",
        "jacket": "#746AA1",
        "jacket_hi": "#9689C7",
        "jacket_shadow": "#514B78",
        "shirt": "#EFE9DA",
        "pants": "#353249",
        "accent": "#86D3E7",
        "shoe": "#333143",
    },
]


def draw_shadow(image):
    layer = Image.new("RGBA", image.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(layer)
    ellipse(draw, (176, 1090, 470, 1160), (4, 8, 14, 115))
    layer = layer.filter(ImageFilter.GaussianBlur(15 * SCALE))
    image.alpha_composite(layer)


def draw_coat_tails(draw, cfg, outline):
    v = cfg["variant"]
    jacket = rgba(cfg["jacket"])
    shadow = rgba(cfg["jacket_shadow"])
    if v == 2:
        shape(draw, [("M", 239, 520), ("C", 220, 640, 210, 790, 222, 894),
                     ("C", 241, 875, 267, 842, 288, 794), ("L", 305, 601), ("Z",)],
              shadow, outline, 4)
        shape(draw, [("M", 398, 515), ("C", 426, 651, 433, 794, 418, 900),
                     ("C", 393, 867, 367, 827, 346, 783), ("L", 333, 598), ("Z",)],
              jacket, outline, 4)
    elif v == 4:
        shape(draw, [("M", 247, 525), ("C", 213, 654, 205, 813, 224, 923),
                     ("L", 301, 836), ("L", 309, 606), ("Z",)], shadow, outline, 4)
        shape(draw, [("M", 391, 523), ("C", 431, 658, 439, 819, 415, 928),
                     ("L", 342, 834), ("L", 331, 606), ("Z",)], jacket, outline, 4)
        shape(draw, [("M", 414, 390), ("C", 480, 485, 489, 661, 448, 803),
                     ("C", 438, 731, 409, 647, 373, 590), ("Z",)],
              rgba(cfg["jacket_hi"], 210), outline, 4)


def draw_back_hair(draw, cfg, outline):
    v = cfg["variant"]
    hair = rgba(cfg["hair"])
    hi = rgba(cfg["hair_hi"])
    if v == 0:
        # A complete, balanced cranium sits behind the face so no side reads as missing.
        shape(draw, [("M", 251, 155), ("C", 246, 101, 275, 57, 319, 47),
                     ("C", 365, 37, 401, 69, 405, 119),
                     ("C", 409, 166, 398, 207, 379, 238),
                     ("C", 360, 257, 285, 258, 264, 238),
                     ("C", 248, 213, 243, 178, 251, 155), ("Z",)],
              hair, outline, 4)
        shape(draw, [("M", 279, 78), ("C", 309, 49, 354, 48, 386, 76),
                     ("C", 357, 65, 320, 70, 288, 98), ("Z",)], hi, None, 0)
    elif v == 2:
        shape(draw, [("M", 258, 105), ("C", 263, 53, 306, 35, 347, 49),
                     ("C", 401, 67, 416, 126, 404, 214),
                     ("C", 417, 305, 407, 382, 383, 432),
                     ("L", 349, 359), ("L", 286, 361),
                     ("C", 245, 315, 234, 208, 258, 105), ("Z",)], hair, outline, 4)
        shape(draw, [("M", 372, 274), ("C", 427, 315, 429, 413, 391, 471),
                     ("C", 384, 408, 360, 346, 338, 307), ("Z",)], hi, outline, 3)
    elif v == 4:
        shape(draw, [("M", 250, 112), ("C", 259, 45, 307, 24, 357, 45),
                     ("C", 419, 72, 422, 163, 404, 250),
                     ("C", 430, 340, 426, 463, 391, 542),
                     ("L", 352, 451), ("L", 292, 449),
                     ("L", 249, 544), ("C", 224, 424, 228, 304, 245, 243), ("Z",)],
              hair, outline, 4)
        shape(draw, [("M", 378, 81), ("C", 420, 161, 398, 361, 377, 468),
                     ("L", 349, 419), ("C", 366, 308, 375, 189, 345, 71), ("Z",)], hi, None, 0)
    else:
        shape(draw, [("M", 252, 113), ("C", 261, 56, 304, 37, 346, 49),
                     ("C", 399, 65, 416, 126, 400, 219),
                     ("C", 392, 265, 372, 298, 350, 318),
                     ("L", 289, 315), ("C", 249, 282, 236, 207, 252, 113), ("Z",)],
              hair, outline, 4)
        shape(draw, [("M", 349, 56), ("C", 387, 97, 392, 181, 365, 239),
                     ("C", 372, 167, 348, 111, 323, 74), ("Z",)], hi, None, 0)


def draw_legs(draw, cfg, outline):
    pants = rgba(cfg["pants"])
    pants_hi = mix(pants, (255, 255, 255, 255), 0.10)
    pants_shadow = mix(pants, (0, 0, 0, 255), 0.28)
    shoe = rgba(cfg["shoe"])
    sole = mix(shoe, (225, 236, 235, 255), 0.28)

    shape(draw, [("M", 264, 600), ("C", 252, 700, 249, 826, 245, 957),
                 ("C", 242, 1018, 236, 1065, 224, 1090),
                 ("L", 291, 1095), ("C", 304, 1027, 309, 932, 315, 828),
                 ("L", 326, 616), ("Z",)], pants, outline, 4)
    shape(draw, [("M", 321, 614), ("L", 339, 814),
                 ("C", 349, 914, 361, 1016, 369, 1087),
                 ("L", 435, 1088), ("C", 418, 979, 409, 870, 402, 764),
                 ("C", 396, 675, 388, 621, 376, 596), ("Z",)], pants_hi, outline, 4)
    shape(draw, [("M", 369, 663), ("C", 374, 793, 389, 962, 405, 1085),
                 ("L", 435, 1088), ("C", 418, 979, 409, 870, 402, 764),
                 ("C", 396, 675, 388, 621, 376, 596), ("Z",)], pants_shadow, None, 0)
    line(draw, [(320, 645), (316, 825)], mix(pants, (255, 255, 255, 255), 0.18), 2)
    line(draw, [(341, 824), (359, 1043)], mix(pants, (255, 255, 255, 255), 0.16), 2)
    line(draw, [(254, 830), (278, 838), (303, 831)], mix(pants, (255, 255, 255, 255), 0.13), 1.7)
    line(draw, [(347, 829), (372, 837), (397, 830)], mix(pants, (255, 255, 255, 255), 0.13), 1.7)

    shape(draw, [("M", 222, 1075), ("C", 205, 1083, 184, 1104, 175, 1121),
                 ("C", 196, 1135, 250, 1138, 304, 1129),
                 ("L", 293, 1090), ("C", 266, 1083, 244, 1081, 222, 1075), ("Z",)],
          shoe, outline, 4)
    shape(draw, [("M", 368, 1078), ("C", 357, 1093, 349, 1110, 347, 1125),
                 ("C", 374, 1137, 424, 1138, 475, 1127),
                 ("C", 461, 1105, 447, 1090, 432, 1080), ("Z",)], shoe, outline, 4)
    line(draw, [(183, 1123), (302, 1128)], sole, 6)
    line(draw, [(351, 1124), (472, 1127)], sole, 6)
    line(draw, [(224, 1098), (278, 1102)], mix(shoe, (255, 255, 255, 255), 0.35), 3)
    line(draw, [(378, 1099), (429, 1101)], mix(shoe, (255, 255, 255, 255), 0.35), 3)


def draw_back_arm(draw, cfg, outline, skin):
    jacket = rgba(cfg["jacket"])
    jacket_shadow = rgba(cfg["jacket_shadow"])
    # Bent pocket arm: tapered at the elbow and clearly attached to the shoulder.
    shape(draw, [("M", 398, 350), ("C", 425, 359, 444, 389, 449, 432),
                 ("C", 454, 477, 437, 521, 402, 570),
                 ("L", 382, 551), ("C", 405, 507, 419, 471, 414, 438),
                 ("C", 409, 402, 392, 384, 374, 379), ("Z",)], jacket_shadow, outline, 4)
    line(draw, [(401, 552), (381, 573), (365, 579)], mix(jacket, (255, 255, 255, 255), 0.20), 3)

def draw_neck(draw, cfg, outline, skin):
    shadow = mix(skin, (111, 62, 46, 255), 0.22)
    shape(draw, [("M", 292, 270), ("C", 297, 297, 293, 319, 282, 337),
                 ("C", 297, 359, 342, 366, 364, 336),
                 ("C", 349, 318, 345, 295, 350, 268), ("Z",)], skin, outline, 3)
    shape(draw, [("M", 319, 280), ("C", 320, 321, 337, 343, 359, 346),
                 ("C", 347, 323, 344, 296, 350, 268), ("Z",)], shadow, None, 0)


def draw_torso(draw, cfg, outline):
    v = cfg["variant"]
    jacket = rgba(cfg["jacket"])
    jacket_hi = rgba(cfg["jacket_hi"])
    jacket_shadow = rgba(cfg["jacket_shadow"])
    shirt = rgba(cfg["shirt"])
    accent = rgba(cfg["accent"])

    shape(draw, [("M", 284, 330), ("C", 259, 334, 232, 345, 214, 367),
                 ("C", 224, 443, 235, 519, 247, 605),
                 ("C", 269, 632, 298, 644, 321, 641),
                 ("C", 348, 645, 379, 633, 399, 603),
                 ("C", 411, 521, 421, 438, 424, 372),
                 ("C", 402, 346, 378, 335, 356, 330),
                 ("C", 339, 352, 303, 352, 284, 330), ("Z",)], jacket, outline, 5)

    shape(draw, [("M", 215, 370), ("C", 230, 355, 252, 344, 281, 336),
                 ("C", 263, 422, 259, 529, 273, 623),
                 ("C", 253, 616, 245, 602, 243, 579), ("Z",)], jacket_hi, None, 0)
    shape(draw, [("M", 356, 332), ("C", 387, 341, 410, 354, 424, 373),
                 ("C", 419, 455, 410, 541, 396, 606),
                 ("C", 383, 624, 369, 632, 350, 637),
                 ("C", 367, 525, 370, 416, 356, 332), ("Z",)], jacket_shadow, None, 0)

    # Fitted inner shirt and lapels; avoids the oversized white triangle from the old model.
    shape(draw, [("M", 294, 344), ("C", 303, 359, 337, 359, 348, 344),
                 ("L", 365, 542), ("C", 346, 558, 294, 558, 274, 540), ("Z",)],
          shirt, outline, 3)
    shape(draw, [("M", 282, 336), ("L", 313, 387), ("L", 276, 454),
                 ("L", 252, 364), ("Z",)], jacket_hi, outline, 3)
    shape(draw, [("M", 357, 335), ("L", 326, 388), ("L", 364, 454),
                 ("L", 389, 361), ("Z",)], jacket_shadow, outline, 3)

    # Waist construction, belt and subtle folds.
    shape(draw, [("M", 247, 577), ("C", 278, 590, 365, 591, 397, 575),
                 ("L", 399, 614), ("C", 367, 638, 272, 638, 247, 608), ("Z",)],
          jacket_shadow, outline, 3)
    rounded_rectangle(draw, (270, 596, 378, 619), 7, mix(jacket_shadow, (0, 0, 0, 255), 0.15), outline, 2)
    rounded_rectangle(draw, (314, 598, 337, 617), 4, accent, outline, 2)
    line(draw, [(281, 538), (298, 557)], mix(jacket_hi, jacket, 0.45), 2)
    line(draw, [(363, 538), (347, 558)], mix(jacket_shadow, jacket, 0.42), 2)
    line(draw, [(319, 424), (320, 573)], mix(jacket_shadow, shirt, 0.30), 1.6)

    if v == 0:
        line(draw, [(253, 350), (377, 574)], accent, 10)
        rounded_rectangle(draw, (374, 465, 424, 526), 8, accent, outline, 3)
        line(draw, [(290, 474), (351, 474)], mix(shirt, (0, 0, 0, 255), 0.18), 3)
        shape(draw, [("M", 382, 548), ("C", 394, 549, 402, 560, 400, 574),
                     ("C", 397, 587, 386, 593, 375, 586),
                     ("C", 375, 573, 377, 559, 382, 548), ("Z",)],
              rgba("#202C39"), outline, 2.5)
        line(draw, [(382, 568), (396, 569)], rgba("#536477"), 1.5)
        line(draw, [(381, 576), (393, 580)], rgba("#536477"), 1.3)
    elif v == 1:
        shape(draw, [("M", 275, 365), ("L", 321, 405), ("L", 364, 365),
                     ("L", 353, 427), ("L", 287, 427), ("Z",)],
              mix(jacket_shadow, accent, 0.35), outline, 2)
        line(draw, [(319, 406), (319, 570)], accent, 4)
        rounded_rectangle(draw, (376, 476, 414, 520), 7, accent, outline, 2)
    elif v == 2:
        shape(draw, [("M", 287, 344), ("C", 309, 369, 335, 369, 354, 344),
                     ("L", 344, 412), ("L", 299, 412), ("Z",)],
              rgba("#6D4731"), outline, 3)
        line(draw, [(389, 373), (255, 584)], rgba("#B88A54"), 9)
        rounded_rectangle(draw, (198, 561, 272, 642), 9, rgba("#745137"), outline, 3)
        line(draw, [(306, 452), (346, 452)], mix(shirt, (0, 0, 0, 255), 0.2), 3)
    elif v == 3:
        shape(draw, [("M", 274, 355), ("C", 293, 383, 348, 384, 368, 354),
                     ("L", 350, 410), ("L", 291, 410), ("Z",)],
              rgba("#242631"), outline, 3)
        line(draw, [(281, 349), (305, 397)], accent, 4)
        line(draw, [(361, 349), (337, 397)], accent, 4)
        rounded_rectangle(draw, (291, 500, 353, 536), 10, accent, outline, 2)
    elif v == 4:
        shape(draw, [("M", 284, 344), ("L", 319, 398), ("L", 355, 344),
                     ("L", 343, 458), ("L", 297, 458), ("Z",)],
              shirt, outline, 3)
        shape(draw, [("M", 320, 392), ("L", 347, 428), ("L", 320, 486),
                     ("L", 292, 428), ("Z",)], accent, outline, 2)
        ellipse(draw, (309, 412, 331, 434), rgba("#E9F8FA"), outline, 2)


def draw_front_arm(draw, cfg, outline, skin):
    jacket = rgba(cfg["jacket"])
    jacket_hi = rgba(cfg["jacket_hi"])
    # Slim relaxed arm with a real elbow break and a narrow cuff.
    shape(draw, [("M", 225, 359), ("C", 202, 371, 189, 404, 185, 448),
                 ("C", 179, 515, 183, 592, 193, 672),
                 ("L", 218, 669), ("C", 213, 594, 210, 530, 214, 477),
                 ("C", 217, 432, 231, 399, 248, 383), ("Z",)], jacket_hi, outline, 4)
    shape(draw, [("M", 192, 661), ("C", 187, 682, 189, 713, 198, 733),
                 ("C", 208, 742, 221, 732, 222, 710),
                 ("C", 223, 687, 214, 669, 192, 661), ("Z",)], skin, outline, 3)
    line(draw, [(192, 655), (216, 652)], rgba(cfg["jacket_shadow"]), 5)
    line(draw, [(199, 700), (200, 727)], mix(skin, outline, 0.28), 1.5)
    line(draw, [(207, 699), (208, 730)], mix(skin, outline, 0.28), 1.5)
    line(draw, [(190, 489), (212, 486)], mix(jacket, (255, 255, 255, 255), 0.18), 2)

def draw_head(draw, cfg, outline, skin):
    v = cfg["variant"]
    skin_shadow = mix(skin, (118, 66, 48, 255), 0.22)
    skin_light = mix(skin, (255, 236, 216, 255), 0.22)
    face_shadow = mix(skin, skin_shadow, 0.34)
    face_light = mix(skin, skin_light, 0.44)
    hair = rgba(cfg["hair"])
    hair_hi = rgba(cfg["hair_hi"])
    accent = rgba(cfg["accent"])

    # Ears are small and sit behind the jaw.
    ellipse(draw, (241, 174, 271, 233), skin, outline, 3)
    ellipse(draw, (372, 174, 402, 233), skin, outline, 3)
    line(draw, [(253, 192), (259, 199), (254, 211)], mix(skin_shadow, outline, 0.12), 1.5)
    line(draw, [(390, 190), (384, 198), (389, 210)], mix(skin_shadow, outline, 0.12), 1.5)

    shape(draw, [("M", 273, 103), ("C", 294, 85, 347, 82, 371, 105),
                 ("C", 394, 127, 394, 192, 381, 234),
                 ("C", 369, 268, 341, 291, 320, 294),
                 ("C", 294, 290, 269, 268, 258, 234),
                 ("C", 246, 194, 249, 130, 273, 103), ("Z",)], skin, outline, 4)
    shape(draw, [("M", 355, 101), ("C", 382, 137, 383, 218, 356, 262),
                 ("C", 346, 278, 333, 288, 320, 294),
                 ("C", 351, 287, 371, 264, 381, 234),
                 ("C", 394, 192, 394, 127, 371, 105), ("Z",)], face_shadow, None, 0)
    shape(draw, [("M", 274, 124), ("C", 263, 161, 265, 223, 282, 252),
                 ("C", 286, 224, 283, 172, 294, 131), ("Z",)], face_light, None, 0)

    # Mature anime eyes: narrow upper lid, restrained iris and clear brows.
    brow = mix(hair, (0, 0, 0, 255), 0.12)
    line(draw, [(276, 171), (293, 165), (309, 169)], brow, 5)
    line(draw, [(334, 168), (351, 164), (368, 171)], brow, 5)
    line(draw, [(276, 196), (291, 191), (309, 196)], outline, 4)
    line(draw, [(333, 196), (351, 190), (369, 196)], outline, 4)
    ellipse(draw, (288, 190, 300, 207), rgba("#5EA0B4"), outline, 1)
    ellipse(draw, (345, 189, 357, 206), rgba("#5EA0B4"), outline, 1)
    ellipse(draw, (292, 194, 298, 204), rgba("#101923"))
    ellipse(draw, (349, 193, 355, 203), rgba("#101923"))
    ellipse(draw, (293, 193, 296, 197), rgba("#F4FBFF"))
    ellipse(draw, (350, 192, 353, 196), rgba("#F4FBFF"))
    line(draw, [(318, 201), (313, 226), (321, 233)], mix(skin_shadow, outline, 0.22), 2)
    line(draw, [(306, 253), (320, 256), (335, 251)], rgba("#8F514B"), 2.4)
    line(draw, [(312, 264), (327, 263)], mix(skin_shadow, skin, 0.58), 1.2)

    # Hairline and front strands leave the cheeks and jaw visibly open.
    if v == 0:
        shape(draw, [("M", 257, 153), ("C", 252, 101, 277, 65, 316, 57),
                     ("C", 353, 49, 388, 75, 398, 117),
                     ("C", 377, 103, 359, 96, 346, 93),
                     ("L", 361, 143), ("L", 333, 113), ("L", 318, 166),
                     ("L", 296, 121), ("L", 276, 160), ("L", 261, 194), ("Z",)],
              hair, outline, 4)
        shape(draw, [("M", 286, 74), ("C", 313, 58, 350, 62, 374, 86),
                     ("C", 343, 79, 313, 87, 292, 106), ("Z",)], hair_hi, None, 0)
        shape(draw, [("M", 256, 151), ("C", 249, 194, 254, 239, 270, 265),
                     ("L", 278, 220), ("L", 274, 160), ("Z",)], hair, outline, 3)
        shape(draw, [("M", 389, 134), ("C", 400, 176, 394, 230, 377, 262),
                     ("L", 372, 214), ("L", 376, 153), ("Z",)], hair, outline, 3)
    elif v == 1:
        shape(draw, [("M", 257, 153), ("C", 258, 93, 292, 57, 338, 59),
                     ("C", 375, 61, 400, 88, 399, 128),
                     ("L", 367, 100), ("L", 351, 142), ("L", 326, 104),
                     ("L", 298, 151), ("L", 272, 128), ("L", 260, 191), ("Z",)],
              hair, outline, 4)
        shape(draw, [("M", 339, 60), ("C", 369, 69, 389, 87, 397, 116),
                     ("C", 372, 101, 350, 97, 329, 99), ("Z",)], hair_hi, None, 0)
        rounded_rectangle(draw, (269, 184, 312, 211), 10, rgba("#15262C", 180), accent, 2)
        rounded_rectangle(draw, (332, 183, 375, 210), 10, rgba("#15262C", 180), accent, 2)
        line(draw, [(312, 196), (332, 195)], accent, 3)
    elif v == 2:
        shape(draw, [("M", 254, 152), ("C", 252, 88, 286, 49, 334, 50),
                     ("C", 374, 52, 403, 82, 404, 128),
                     ("L", 376, 105), ("L", 363, 155), ("L", 340, 111),
                     ("L", 314, 158), ("L", 287, 114), ("L", 263, 184), ("Z",)],
              hair, outline, 4)
        shape(draw, [("M", 281, 73), ("C", 319, 49, 364, 61, 390, 94),
                     ("C", 350, 72, 314, 78, 281, 112), ("Z",)], hair_hi, None, 0)
        line(draw, [(275, 214), (279, 272)], hair, 8)
        line(draw, [(373, 209), (367, 277)], hair, 8)
    elif v == 3:
        shape(draw, [("M", 254, 158), ("C", 257, 94, 295, 58, 341, 60),
                     ("C", 382, 62, 403, 92, 399, 136),
                     ("L", 368, 108), ("L", 351, 153), ("L", 327, 110),
                     ("L", 303, 158), ("L", 278, 120), ("L", 259, 190), ("Z",)],
              hair, outline, 4)
        shape(draw, [("M", 313, 64), ("C", 341, 54, 374, 72, 391, 99),
                     ("C", 360, 83, 335, 87, 309, 106), ("Z",)], hair_hi, None, 0)
        ellipse(draw, (225, 155, 269, 230), rgba("#242733"), outline, 4)
        ellipse(draw, (374, 155, 418, 230), rgba("#242733"), outline, 4)
        ellipse(draw, (237, 168, 261, 216), accent, None, 0)
        ellipse(draw, (382, 168, 406, 216), accent, None, 0)
        line(draw, [(246, 157), (275, 117), (320, 102), (369, 117), (397, 158)], outline, 5)
    else:
        shape(draw, [("M", 252, 157), ("C", 253, 84, 292, 40, 340, 45),
                     ("C", 382, 49, 408, 84, 404, 136),
                     ("L", 377, 108), ("L", 365, 158), ("L", 341, 108),
                     ("L", 320, 162), ("L", 293, 112), ("L", 260, 195), ("Z",)],
              hair, outline, 4)
        shape(draw, [("M", 274, 72), ("C", 315, 41, 365, 55, 395, 91),
                     ("C", 355, 69, 317, 77, 283, 112), ("Z",)], hair_hi, None, 0)
        shape(draw, [("M", 264, 82), ("L", 289, 28), ("L", 314, 80), ("Z",)], accent, outline, 3)
        shape(draw, [("M", 351, 79), ("L", 376, 27), ("L", 397, 92), ("Z",)], accent, outline, 3)
        ellipse(draw, (310, 74, 332, 96), rgba("#E9FBFF"), outline, 2)


def draw_city_head(draw, cfg, outline, skin):
    skin_shadow = mix(skin, (112, 62, 46, 255), 0.19)
    skin_light = mix(skin, (255, 238, 220, 255), 0.18)
    face_shadow = mix(skin, skin_shadow, 0.25)
    face_light = mix(skin, skin_light, 0.35)
    hair = rgba(cfg["hair"])
    hair_hi = rgba(cfg["hair_hi"])
    accent = rgba(cfg["accent"])

    # Balanced ears and a wider adult jaw establish the skull before any fringe is added.
    ellipse(draw, (245, 174, 273, 228), skin, outline, 3)
    ellipse(draw, (372, 174, 400, 228), skin, outline, 3)
    line(draw, [(256, 190), (262, 197), (257, 210)], mix(skin_shadow, outline, 0.10), 1.3)
    line(draw, [(389, 190), (383, 197), (388, 210)], mix(skin_shadow, outline, 0.10), 1.3)

    shape(draw, [("M", 276, 106), ("C", 296, 88, 347, 87, 369, 107),
                 ("C", 389, 128, 392, 181, 381, 223),
                 ("C", 373, 253, 350, 281, 322, 292),
                 ("C", 293, 283, 270, 254, 261, 223),
                 ("C", 251, 183, 255, 129, 276, 106), ("Z",)], skin, outline, 4)
    shape(draw, [("M", 356, 101), ("C", 382, 130, 385, 190, 374, 229),
                 ("C", 365, 258, 344, 282, 322, 292),
                 ("C", 349, 281, 372, 253, 381, 223),
                 ("C", 392, 181, 389, 128, 369, 107), ("Z",)], face_shadow, None, 0)
    shape(draw, [("M", 275, 127), ("C", 264, 160, 267, 214, 281, 244),
                 ("C", 286, 214, 284, 168, 295, 126), ("Z",)], face_light, None, 0)

    # Clear adult anime features without lower-eye shadows or cheek smudges.
    brow = mix(hair, (0, 0, 0, 255), 0.08)
    line(draw, [(275, 172), (292, 165), (309, 169)], brow, 4.5)
    line(draw, [(335, 169), (352, 164), (369, 170)], brow, 4.5)
    line(draw, [(276, 194), (292, 189), (309, 193)], outline, 3.5)
    line(draw, [(335, 193), (352, 188), (369, 193)], outline, 3.5)
    ellipse(draw, (289, 188, 300, 204), rgba("#4B829A"), outline, 1)
    ellipse(draw, (346, 187, 357, 203), rgba("#4B829A"), outline, 1)
    ellipse(draw, (293, 192, 298, 201), rgba("#111923"))
    ellipse(draw, (350, 191, 355, 200), rgba("#111923"))
    ellipse(draw, (294, 190, 297, 194), rgba("#F4FBFF"))
    ellipse(draw, (351, 189, 354, 193), rgba("#F4FBFF"))
    line(draw, [(320, 198), (315, 223), (322, 231)], mix(skin_shadow, outline, 0.14), 1.8)
    line(draw, [(309, 251), (322, 254), (335, 250)], rgba("#87504A"), 2.1)

    # A full rounded cap, then separate fringe pieces and symmetric sideburns.
    shape(draw, [("M", 252, 158), ("C", 248, 101, 277, 58, 320, 49),
                 ("C", 364, 40, 400, 72, 403, 123),
                 ("C", 387, 108, 369, 99, 350, 96),
                 ("C", 319, 91, 285, 101, 260, 129),
                 ("C", 256, 139, 253, 149, 252, 158), ("Z",)], hair, None, 0)
    shape(draw, [("M", 279, 75), ("C", 309, 52, 352, 51, 383, 76),
                 ("C", 350, 67, 316, 74, 288, 98), ("Z",)], hair_hi, None, 0)
    shape(draw, [("M", 263, 125), ("C", 276, 111, 289, 102, 303, 98),
                 ("L", 289, 160), ("L", 276, 143), ("L", 261, 181), ("Z",)], hair, None, 0)
    shape(draw, [("M", 294, 101), ("C", 310, 95, 326, 93, 341, 96),
                 ("L", 322, 157), ("L", 302, 126), ("L", 287, 168), ("Z",)], hair, None, 0)
    shape(draw, [("M", 332, 96), ("C", 348, 97, 362, 102, 374, 110),
                 ("L", 361, 151), ("L", 341, 119), ("L", 322, 158), ("Z",)], hair, None, 0)
    strand = mix(hair_hi, hair, 0.38)
    line(draw, [(286, 101), (281, 132), (278, 153)], strand, 1.4)
    line(draw, [(318, 95), (316, 125), (322, 153)], strand, 1.4)
    line(draw, [(350, 99), (355, 124), (360, 146)], strand, 1.4)
    shape(draw, [("M", 254, 143), ("C", 247, 168, 250, 204, 263, 229),
                 ("L", 274, 199), ("L", 273, 151), ("Z",)], hair, outline, 3)
    shape(draw, [("M", 391, 140), ("C", 399, 166, 396, 203, 383, 228),
                 ("L", 372, 198), ("L", 374, 149), ("Z",)], hair, outline, 3)
    line(draw, [(260, 151), (266, 124)], accent, 2)


def draw_rim_light(draw, cfg):
    rim = rgba(cfg["accent"], 115)
    line(draw, [(223, 372), (177, 449), (176, 598)], rim, 2)
    line(draw, [(267, 108), (253, 154), (253, 213)], rim, 2)
    line(draw, [(398, 374), (456, 440), (448, 510)], rim, 2)


def render_character(cfg):
    image = Image.new("RGBA", (W * SCALE, H * SCALE), (0, 0, 0, 0))
    draw_shadow(image)
    draw = ImageDraw.Draw(image)
    outline = rgba("#111923")
    skin = rgba(cfg["skin"])

    draw_back_hair(draw, cfg, outline)
    draw_coat_tails(draw, cfg, outline)
    draw_legs(draw, cfg, outline)
    draw_back_arm(draw, cfg, outline, skin)
    draw_neck(draw, cfg, outline, skin)
    draw_torso(draw, cfg, outline)
    draw_front_arm(draw, cfg, outline, skin)
    if cfg["variant"] == 0:
        draw_city_head(draw, cfg, outline, skin)
    else:
        draw_head(draw, cfg, outline, skin)
    draw_rim_light(draw, cfg)

    image = image.resize((W, H), Image.Resampling.LANCZOS)
    image.save(OUT / cfg["file"], optimize=True)
    return OUT / cfg["file"]


if __name__ == "__main__":
    for character in CHARACTERS:
        print(render_character(character))
