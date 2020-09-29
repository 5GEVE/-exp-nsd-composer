package it.cnit.blueprint.composer;

import java.awt.image.BufferedImage;

public class CompareImages {

  /**
   * Compares two images pixel by pixel.
   *
   * @param imgA the first image.
   * @param imgB the second image.
   * @return whether the images are both the same or not.
   */
  public static boolean compare(BufferedImage imgA, BufferedImage imgB) {
    // The images must be the same size.
    if (imgA.getWidth() != imgB.getWidth() || imgA.getHeight() != imgB.getHeight()) {
      return false;
    }

    int width = imgA.getWidth();
    int height = imgA.getHeight();

    // Loop over every pixel.
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        // Compare the pixels for equality.
        if (imgA.getRGB(x, y) != imgB.getRGB(x, y)) {
          return false;
        }
      }
    }

    return true;
  }
}
