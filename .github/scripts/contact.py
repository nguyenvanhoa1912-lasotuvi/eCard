import sys, glob, os
from PIL import Image, ImageDraw
src, dst = sys.argv[1], sys.argv[2]
files = sorted(glob.glob(os.path.join(src, '*.png')))
if not files: sys.exit(0)
ims = [Image.open(f).convert('RGB') for f in files]
tw = 300; th = int(ims[0].height * tw / ims[0].width)
cols = 3; rows = (len(ims) + cols - 1) // cols
sheet = Image.new('RGB', (cols * (tw + 16) + 16, rows * (th + 36) + 16), (15, 22, 38))
d = ImageDraw.Draw(sheet)
for i, (im, f) in enumerate(zip(ims, files)):
    x = 16 + (i % cols) * (tw + 16); y = 16 + (i // cols) * (th + 36)
    sheet.paste(im.resize((tw, th)), (x, y + 20))
    d.text((x, y + 4), os.path.basename(f)[3:-4], fill=(232, 194, 90))
sheet.save(dst)
