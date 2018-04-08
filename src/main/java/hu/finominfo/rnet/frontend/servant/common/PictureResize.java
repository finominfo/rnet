package hu.finominfo.rnet.frontend.servant.common;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.TimeUnit;

import static hu.finominfo.rnet.common.Utils.getStackTrace;

/**
 * Created by kks on 2018.04.07..
 */
public class PictureResize {
    private static PictureResize ourInstance = new PictureResize();
    private final static Logger logger = Logger.getLogger(PictureResize.class);

    public static PictureResize get() {
        return ourInstance;
    }

    private PictureResize() {
        Globals.get().executor.schedule(() -> {
            File pictureFolder = new File(Globals.pictureFolder);
            if (pictureFolder.exists()) {
                File resizedPictureFolder = new File(Globals.resizedPictureFolder);
                if (!resizedPictureFolder.exists()) {
                    if (resizedPictureFolder.mkdir()) {
                        logger.info(resizedPictureFolder.getAbsolutePath() + " created successfully.");
                    } else {
                        logger.error(resizedPictureFolder.getAbsolutePath() + " could not be created.");
                        return;
                    }
                }
                Globals.get().executor.submit(() -> checkAllFiles());
            } else {
                logger.info("There is no " + Globals.pictureFolder + " folder.");
            }
        }, 20, TimeUnit.SECONDS);
    }

    private void checkAllFiles() {
        Utils.getFilesFromFolder(Globals.pictureFolder).stream().forEach(name -> {
            getResizedImage(Globals.pictureFolder + File.separator + name, name);
            logger.info(name + " was checked well.");
        });
    }

    public Image getResizedImage(final String pathAndName, final String name) {
        final String resizedPathAndName = Globals.resizedPictureFolder + File.separator + name;
        File resizedPathAndNameFile = new File(resizedPathAndName);
        File pathAndNameFile = new File(pathAndName);
        if (resizedPathAndNameFile.exists() && resizedPathAndNameFile.lastModified() > pathAndNameFile.lastModified()) {
            try {
                return ImageIO.read(resizedPathAndNameFile);
            } catch (Exception e) {
                logger.error(getStackTrace(e));
            }
        }
        BufferedImage originalImage = null;
        try {
            originalImage = ImageIO.read(pathAndNameFile);
        } catch (Exception e) {
            logger.error(getStackTrace(e));
            return null;
        }
        int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        double widthScale = ((double) Globals.get().width) / (double) width;
        double heightScale = ((double) Globals.get().height) / (double) height;
        double resize = widthScale < heightScale ? widthScale : heightScale;
        int x = (int) (width * resize);
        int y = (int) (height * resize);
        BufferedImage scaledInstance = resizeImage(originalImage, type, x, y);
        Globals.get().executor.submit(() -> saveImage(resizedPathAndNameFile, scaledInstance));
        return scaledInstance;
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int type, int IMG_WIDTH, int IMG_HEIGHT) {
        BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
        g.dispose();
        return resizedImage;
    }

    public void saveImage(File resizedPathAndNameFile, BufferedImage scaledInstance) {
        String extension = "";
        int i = resizedPathAndNameFile.getName().lastIndexOf('.');
        if (i > 0) {
            extension = resizedPathAndNameFile.getName().substring(i + 1).toLowerCase();
        }
        try {
            ImageIO.write(scaledInstance, extension, resizedPathAndNameFile);
        } catch (Exception e) {
            logger.error(getStackTrace(e));
        }
    }
}
