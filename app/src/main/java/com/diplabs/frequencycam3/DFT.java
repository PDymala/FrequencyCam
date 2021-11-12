package com.diplabs.frequencycam3;


import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Modified version of:
 * <p>
 * The controller associated to the only view of our application. The
 * application logic is implemented here. It handles the button for opening an
 * image and perform all the operation related to the Fourier transformation and
 * antitransformation.
 *
 * @author Piotr Dymala mod Luigi De Russis
 * @version X (2021-01-22)
 * @since 1.0 (2013-12-11)
 */
public class DFT {

    // support variables

    public DFT() {

    }

    Mat realResult;
    Mat padded;
    List<Mat> planes;
    Mat complexI;
    Mat complexI2;
    Mat mag;

    Mat magI;
    Mat magI2;
    Mat magI3;
    Mat magI4;
    Mat magI5;
    Mat crop;

    Mat q0;
    Mat q1;
    Mat q2;
    Mat q3;

    Mat tmp ;

    public void release(){

        padded.release();

        for (Mat e : planes){
            e.release();
        }

        complexI.release();
        complexI2.release();
        mag.release();

        magI.release();
        magI2.release();
        magI3.release();
        magI4.release();
        magI5.release();
        q0.release();
        q1.release();
        q2.release();
        q3.release();
        tmp.release();
        realResult.release();

        System.gc();
        System.runFinalization();
    }

    public Mat getDFT(Mat singleChannel, boolean centralize, boolean matrix) {

        singleChannel.convertTo(singleChannel, CvType.CV_64FC1);

        int m = Core.getOptimalDFTSize(singleChannel.rows());
        int n = Core.getOptimalDFTSize(singleChannel.cols()); // on the border
        // add zero
        // values
        // Imgproc.copyMakeBorder(image1,
        // padded, 0, m -
        // image1.rows(), 0, n

        padded = new Mat(new Size(n, m), CvType.CV_64FC1); // expand input
        // image to
        // optimal size

        Core.copyMakeBorder(singleChannel, padded, 0, m - singleChannel.rows(), 0,
                n - singleChannel.cols(), Core.BORDER_CONSTANT);

         planes = new ArrayList<Mat>();
        planes.add(padded);
        planes.add(Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC1));

         complexI = Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC2);

         complexI2 = Mat
                .zeros(padded.rows(), padded.cols(), CvType.CV_64FC2);

        Core.merge(planes, complexI); // Add to the expanded another plane with
        // zeros

        Core.dft(complexI, complexI2); // this way the result may fit in the
        // source matrix

        // compute the magnitude and switch to logarithmic scale
        // => log(1 + sqrt(Re(DFT(I))^2 + Im(DFT(I))^2))
        Core.split(complexI2, planes); // planes[0] = Re(DFT(I), planes[1] =
        // Im(DFT(I))

        mag = new Mat(planes.get(0).size(), planes.get(0).type());

        Core.magnitude(planes.get(0), planes.get(1), mag);// planes[0]
        // =
        // magnitude

         magI = mag;
         magI2 = new Mat(magI.size(), magI.type());
         magI3 = new Mat(magI.size(), magI.type());
         magI4 = new Mat(magI.size(), magI.type());
         magI5 = new Mat(magI.size(), magI.type());

        Core.add(magI, Mat.ones(padded.rows(), padded.cols(), CvType.CV_64FC1),
                magI2); // switch to logarithmic scale
        Core.log(magI2, magI3);

        crop = new Mat(magI3, new Rect(0, 0, magI3.cols() & -2,
                magI3.rows() & -2));

        magI4 = crop.clone();   //hmmmmmmmmmmmmmmmmmmmmmmmmmm

        // rearrange the quadrants of Fourier image so that the origin is at the
        // image center
        int cx = magI4.cols() / 2;
        int cy = magI4.rows() / 2;

        Rect q0Rect = new Rect(0, 0, cx, cy);
        Rect q1Rect = new Rect(cx, 0, cx, cy);
        Rect q2Rect = new Rect(0, cy, cx, cy);
        Rect q3Rect = new Rect(cx, cy, cx, cy);

         q0 = new Mat(magI4, q0Rect); // Top-Left - Create a ROI per quadrant
         q1 = new Mat(magI4, q1Rect); // Top-Right
         q2 = new Mat(magI4, q2Rect); // Bottom-Left
         q3 = new Mat(magI4, q3Rect); // Bottom-Right

         tmp = new Mat(); // swap quadrants (Top-Left with Bottom-Right)

       if (centralize){

           q0.copyTo(tmp);
           q3.copyTo(q0);
           tmp.copyTo(q3);

           q1.copyTo(tmp); // swap quadrant (Top-Right with Bottom-Left)
           q2.copyTo(q1);
           tmp.copyTo(q2);

       }



       if (matrix){
           Core.normalize(magI4, magI5, 0, 255, Core.NORM_MINMAX);

         realResult = new Mat(magI5.size(), CvType.CV_8UC1);

        magI5.convertTo(realResult, CvType.CV_8UC1);

       }
       else {

           Core.normalize(q0, q0, 0, 255, Core.NORM_MINMAX);
           realResult = new Mat(magI5.size(), CvType.CV_8UC1);
           q0.convertTo(realResult, CvType.CV_8UC1);


       }



        return realResult;
    }


    public Mat getPhase2(Mat singleChannel, boolean centralize, boolean matrix) {

        singleChannel.convertTo(singleChannel, CvType.CV_64FC1);

        int m = Core.getOptimalDFTSize(singleChannel.rows());
        int n = Core.getOptimalDFTSize(singleChannel.cols()); // on the border
        // add zero
        // values
        // Imgproc.copyMakeBorder(image1,
        // padded, 0, m -
        // image1.rows(), 0, n

        padded = new Mat(new Size(n, m), CvType.CV_64FC1); // expand input
        // image to
        // optimal size

        Core.copyMakeBorder(singleChannel, padded, 0, m - singleChannel.rows(), 0,
                n - singleChannel.cols(), Core.BORDER_CONSTANT);

        planes = new ArrayList<Mat>();
        planes.add(padded);
        planes.add(Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC1));

        complexI = Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC2);

        complexI2 = Mat
                .zeros(padded.rows(), padded.cols(), CvType.CV_64FC2);

        Core.merge(planes, complexI); // Add to the expanded another plane with
        // zeros

        Core.dft(complexI, complexI2); // this way the result may fit in the
        // source matrix

        // compute the magnitude and switch to logarithmic scale
        // => log(1 + sqrt(Re(DFT(I))^2 + Im(DFT(I))^2))
        Core.split(complexI2, planes); // planes[0] = Re(DFT(I), planes[1] =
        // Im(DFT(I))

        mag = new Mat(planes.get(0).size(), planes.get(0).type());

       // Core.magnitude(planes.get(0), planes.get(1), mag);// planes[0]
        // =
        // magnitude

        Core.phase(planes.get(0), planes.get(1), mag);






        magI = mag;
        magI2 = new Mat(magI.size(), magI.type());
        magI3 = new Mat(magI.size(), magI.type());
        magI4 = new Mat(magI.size(), magI.type());
        magI5 = new Mat(magI.size(), magI.type());

        Core.add(magI, Mat.ones(padded.rows(), padded.cols(), CvType.CV_64FC1),
                magI2); // switch to logarithmic scale
        Core.log(magI2, magI3);

        crop = new Mat(magI3, new Rect(0, 0, magI3.cols() & -2,
                magI3.rows() & -2));

        magI4 = crop.clone();   //hmmmmmmmmmmmmmmmmmmmmmmmmmm

        // rearrange the quadrants of Fourier image so that the origin is at the
        // image center
        int cx = magI4.cols() / 2;
        int cy = magI4.rows() / 2;

        Rect q0Rect = new Rect(0, 0, cx, cy);
        Rect q1Rect = new Rect(cx, 0, cx, cy);
        Rect q2Rect = new Rect(0, cy, cx, cy);
        Rect q3Rect = new Rect(cx, cy, cx, cy);

        q0 = new Mat(magI4, q0Rect); // Top-Left - Create a ROI per quadrant
        q1 = new Mat(magI4, q1Rect); // Top-Right
        q2 = new Mat(magI4, q2Rect); // Bottom-Left
        q3 = new Mat(magI4, q3Rect); // Bottom-Right

        tmp = new Mat(); // swap quadrants (Top-Left with Bottom-Right)
        if (centralize){

            q0.copyTo(tmp);
            q3.copyTo(q0);
            tmp.copyTo(q3);

            q1.copyTo(tmp); // swap quadrant (Top-Right with Bottom-Left)
            q2.copyTo(q1);
            tmp.copyTo(q2);

        }



        if (matrix){
            Core.normalize(magI4, magI5, 0, 255, Core.NORM_MINMAX);

            realResult = new Mat(magI5.size(), CvType.CV_8UC1);

            magI5.convertTo(realResult, CvType.CV_8UC1);

        }
        else {

            Core.normalize(q0, q0, 0, 255, Core.NORM_MINMAX);
            realResult = new Mat(magI5.size(), CvType.CV_8UC1);
            q0.convertTo(realResult, CvType.CV_8UC1);


        }




        return realResult;
    }


}