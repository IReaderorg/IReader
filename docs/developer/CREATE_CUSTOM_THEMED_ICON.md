# How to Create a Custom Themed Icon for IReader

## Overview

This guide will help you create a custom monochrome icon that supports Material You theming on Android 13+.

## Quick Start

### Option 1: Use Your Existing Foreground Icon

If you already have a vector foreground icon, convert it to monochrome:

1. **Find your current foreground icon** (if it's a vector drawable)
2. **Copy the path data**
3. **Create monochrome version** with black fill
4. **Test and adjust**

### Option 2: Design from Scratch

Create a new icon using vector graphics tools.

## Step-by-Step Guide

### Step 1: Design Your Icon

**Design Requirements:**
- Simple, recognizable shape
- Works well at small sizes (48dp - 192dp)
- Single color (will be tinted by system)
- No fine details that disappear when small
- Represents your app's purpose

**For IReader (book reading app), good options:**
- 📖 Open book
- 📚 Stack of books
- 📄 Document/page
- 🔖 Bookmark
- 👁️ Eye (for reading)

### Step 2: Get Vector Path Data

#### Method A: Use Android Studio

1. **Right-click** `res/drawable` folder
2. Select **New → Vector Asset**
3. Choose **Clip Art** or **Local file (SVG)**
4. Select your icon
5. Click **Next** → **Finish**
6. Open the generated XML file
7. Copy the `android:pathData` value

#### Method B: Use Online Tools

**Recommended Tools:**
- [SVG to Android Vector Drawable](https://svg2vector.com/)
- [Shape Shifter](https://shapeshifter.design/)
- [Material Icons](https://fonts.google.com/icons)

**Process:**
1. Upload/create your SVG icon
2. Convert to Android Vector Drawable
3. Copy the `pathData` attribute

#### Method C: Use Material Icons

1. Go to [Material Icons](https://fonts.google.com/icons)
2. Search for relevant icons (e.g., "book", "menu_book", "auto_stories")
3. Download as Android Vector Drawable
4. Extract the `pathData`

### Step 3: Create the Monochrome Icon File

Edit `android/src/main/res/drawable/ic_launcher_monochrome.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    
    <!-- Center and scale the icon -->
    <group
        android:scaleX="0.6"
        android:scaleY="0.6"
        android:translateX="21.6"
        android:translateY="21.6">
        
        <!-- Your icon path here -->
        <path
            android:fillColor="#000000"
            android:pathData="YOUR_PATH_DATA_HERE" />
    </group>
</vector>
```

**Key Points:**
- `android:width` and `android:height`: Always 108dp
- `android:viewportWidth` and `android:viewportHeight`: Usually 108 or 24
- `android:fillColor`: Always `#000000` (black)
- `android:scaleX/Y`: Adjust to fit safe zone (typically 0.5 - 0.7)
- `android:translateX/Y`: Center the icon (typically 20-25)

### Step 4: Calculate Proper Scaling

**Safe Zone Formula:**
- Canvas: 108dp × 108dp
- Safe zone: 66dp diameter circle (centered)
- If your icon viewport is 24×24:
  - Scale: 66/24 = 2.75 (too large, use 0.6-0.7 of this)
  - Final scale: ~0.6
  - Translation: (108 - 24×scale) / 2

**Common Viewport Sizes:**

| Viewport | Recommended Scale | Translation |
|----------|-------------------|-------------|
| 24×24 | 0.6 | 21.6 |
| 48×48 | 0.5 | 27 |
| 108×108 | 0.6 | 21.6 |

### Step 5: Test Your Icon

1. **Build and install** the app
2. **Enable themed icons** on Android 13+ device
3. **Check at different sizes**:
   - Home screen
   - App drawer
   - Recent apps
   - Settings
4. **Test with different wallpapers**
5. **Verify in light and dark modes**

## Example Icons

### Example 1: Simple Book Icon (Current)

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <group
        android:scaleX="0.6"
        android:scaleY="0.6"
        android:translateX="21.6"
        android:translateY="21.6">
        <path
            android:fillColor="#000000"
            android:pathData="M18,2H6C4.9,2 4,2.9 4,4v16c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V4C20,2.9 19.1,2 18,2zM18,20H6V4h2v9l2.5,-1.5L13,13V4h5V20z" />
    </group>
</vector>
```

### Example 2: Open Book Icon

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <group
        android:scaleX="0.65"
        android:scaleY="0.65"
        android:translateX="18.9"
        android:translateY="18.9">
        <path
            android:fillColor="#000000"
            android:pathData="M21,5c-1.11,-0.35 -2.33,-0.5 -3.5,-0.5c-1.95,0 -4.05,0.4 -5.5,1.5c-1.45,-1.1 -3.55,-1.5 -5.5,-1.5S2.45,4.9 1,6v14.65c0,0.25 0.25,0.5 0.5,0.5c0.1,0 0.15,-0.05 0.25,-0.05C3.1,20.45 5.05,20 6.5,20c1.95,0 4.05,0.4 5.5,1.5c1.35,-0.85 3.8,-1.5 5.5,-1.5c1.65,0 3.35,0.3 4.75,1.05c0.1,0.05 0.15,0.05 0.25,0.05c0.25,0 0.5,-0.25 0.5,-0.5V6C22.4,5.55 21.75,5.25 21,5zM21,18.5c-1.1,-0.35 -2.3,-0.5 -3.5,-0.5c-1.7,0 -4.15,0.65 -5.5,1.5V8c1.35,-0.85 3.8,-1.5 5.5,-1.5c1.2,0 2.4,0.15 3.5,0.5V18.5z" />
        <path
            android:fillColor="#000000"
            android:pathData="M17.5,10.5c0.88,0 1.73,0.09 2.5,0.26V9.24C19.21,9.09 18.36,9 17.5,9c-1.7,0 -3.24,0.29 -4.5,0.83v1.66C14.13,10.85 15.7,10.5 17.5,10.5z" />
        <path
            android:fillColor="#000000"
            android:pathData="M13,12.49v1.66c1.13,-0.64 2.7,-0.99 4.5,-0.99c0.88,0 1.73,0.09 2.5,0.26V11.9c-0.79,-0.15 -1.64,-0.24 -2.5,-0.24C15.8,11.66 14.26,11.96 13,12.49z" />
        <path
            android:fillColor="#000000"
            android:pathData="M17.5,14.33c-1.7,0 -3.24,0.29 -4.5,0.83v1.66c1.13,-0.64 2.7,-0.99 4.5,-0.99c0.88,0 1.73,0.09 2.5,0.26v-1.52C19.21,14.41 18.36,14.33 17.5,14.33z" />
    </group>
</vector>
```

### Example 3: Reading Icon (Eye + Book)

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <group
        android:scaleX="0.7"
        android:scaleY="0.7"
        android:translateX="16.2"
        android:translateY="16.2">
        <path
            android:fillColor="#000000"
            android:pathData="M12,4.5C7,4.5 2.73,7.61 1,12c1.73,4.39 6,7.5 11,7.5s9.27,-3.11 11,-7.5c-1.73,-4.39 -6,-7.5 -11,-7.5zM12,17c-2.76,0 -5,-2.24 -5,-5s2.24,-5 5,-5 5,2.24 5,5 -2.24,5 -5,5zM12,9c-1.66,0 -3,1.34 -3,3s1.34,3 3,3 3,-1.34 3,-3 -1.34,-3 -3,-3z" />
    </group>
</vector>
```

### Example 4: Minimalist "R" Letter

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <group
        android:scaleX="0.8"
        android:scaleY="0.8"
        android:translateX="10.8"
        android:translateY="10.8">
        <path
            android:fillColor="#000000"
            android:pathData="M30,20 L30,80 L45,80 L45,55 L55,55 L70,80 L87,80 L70,52 Q87,52 87,35 Q87,20 70,20 Z M45,35 L45,42 L65,42 Q70,42 70,38.5 Q70,35 65,35 Z" />
    </group>
</vector>
```

## Advanced Customization

### Multiple Paths

You can combine multiple paths for complex icons:

```xml
<group
    android:scaleX="0.6"
    android:scaleY="0.6"
    android:translateX="21.6"
    android:translateY="21.6">
    
    <!-- Background shape -->
    <path
        android:fillColor="#000000"
        android:pathData="M..." />
    
    <!-- Foreground detail -->
    <path
        android:fillColor="#000000"
        android:pathData="M..." />
</group>
```

### Rotation and Positioning

```xml
<group
    android:scaleX="0.6"
    android:scaleY="0.6"
    android:translateX="21.6"
    android:translateY="21.6"
    android:rotation="45"
    android:pivotX="54"
    android:pivotY="54">
    <!-- Icon paths -->
</group>
```

## Testing Checklist

- [ ] Icon visible at all sizes
- [ ] Icon recognizable when tinted
- [ ] No clipping at edges
- [ ] Works in light theme
- [ ] Works in dark theme
- [ ] Looks good with various wallpapers
- [ ] Maintains brand identity
- [ ] Simple enough for small sizes

## Common Issues and Solutions

### Issue: Icon is too small
**Solution**: Increase `scaleX` and `scaleY` values (e.g., from 0.6 to 0.7)

### Issue: Icon is clipped
**Solution**: Decrease scale or adjust translation to center better

### Issue: Icon is off-center
**Solution**: Adjust `translateX` and `translateY` values

### Issue: Icon has too much detail
**Solution**: Simplify the design, remove fine lines

### Issue: Icon not recognizable when tinted
**Solution**: Increase contrast, simplify shapes, make bolder

## Tools and Resources

### Design Tools
- **Figma** (free): https://figma.com
- **Inkscape** (free): https://inkscape.org
- **Adobe Illustrator** (paid)

### Conversion Tools
- **SVG to Vector Drawable**: https://svg2vector.com/
- **Shape Shifter**: https://shapeshifter.design/

### Icon Resources
- **Material Icons**: https://fonts.google.com/icons
- **Material Design Icons**: https://materialdesignicons.com/
- **Iconify**: https://icon-sets.iconify.design/

### Testing Tools
- **Android Studio Layout Inspector**
- **Adaptive Icon Preview** in Android Studio

## Best Practices

1. **Keep it simple**: Fewer paths = better performance
2. **Test early**: Check on device frequently
3. **Use safe zone**: Keep important elements in 66dp circle
4. **Maintain brand**: Icon should still represent your app
5. **Consider accessibility**: High contrast, clear shapes
6. **Optimize paths**: Remove unnecessary points
7. **Use standard viewports**: 24×24 or 108×108

## Next Steps

1. **Design your icon** using the tools above
2. **Get the vector path data**
3. **Update** `ic_launcher_monochrome.xml`
4. **Test** on Android 13+ device
5. **Iterate** based on feedback

## Need Help?

If you need help with:
- Converting your existing icon
- Creating a custom design
- Troubleshooting issues

Just ask! I can help you create the perfect themed icon for IReader.
