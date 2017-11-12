package sample;

import org.opencv.core.Mat;

/**
 * Created by breku on 11.11.17.
 */
public class DiceCounter {


	int countPips(Mat dice){
//
//		// resize
//		cv::resize(dice, dice, cv::Size(150, 150));
//
//		// convert to grayscale
//		cvtColor(dice, dice, CV_BGR2GRAY);
//
//		// threshold
//		cv::threshold(dice, dice, 150, 255, cv::THRESH_BINARY | CV_THRESH_OTSU );
//
//		// show
//		cv::namedWindow("processed", true);
//		cv::imshow("processed", dice);
//
//
//		// floodfill
//		cv::floodFill(dice, cv::Point(0,0), cv::Scalar(255));
//		cv::floodFill(dice, cv::Point(0,149), cv::Scalar(255));
//		cv::floodFill(dice, cv::Point(149,0), cv::Scalar(255));
//		cv::floodFill(dice, cv::Point(149,149), cv::Scalar(255));
//
//		// search for blobs
//		cv::SimpleBlobDetector::Params params;
//
//		// filter by interia defines how elongated a shape is.
//		params.filterByInertia = true;
//		params.minInertiaRatio = 0.5;
//
//		// will hold our keyponts
//		std::vector<cv::KeyPoint> keypoints;
//
//		// create new blob detector with our parameters
//		cv::Ptr<cv::SimpleBlobDetector> blobDetector = cv::SimpleBlobDetector::create(params);
//
//		// detect blobs
//		blobDetector->detect(dice, keypoints);
//
//
//		// return number of pips
//		return keypoints.size();
		return 1;
	}

}
