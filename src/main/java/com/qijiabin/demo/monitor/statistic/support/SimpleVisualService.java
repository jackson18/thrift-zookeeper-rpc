package com.qijiabin.demo.monitor.statistic.support;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qijiabin.demo.monitor.statistic.VisualService;


/**
 * ========================================================
 * 日 期：2016年4月18日 下午2:49:41
 * 作 者：jiabin.qi
 * 版 本：1.0.0
 * 类说明：数据可视化
 * TODO
 * ========================================================
 * 修订日期     修订人    描述
 */
public class SimpleVisualService implements VisualService{
	
	private static final Logger logger = LoggerFactory.getLogger(SimpleVisualService.class);

	
	/**
	 * 绘制表格
	 */
	@Override
	public void draw() {
		File rootDir = new File(STATISTICS_DIRECTORY);
		if (!rootDir.exists()) {
			return;
		}
		File[] dateDirs = rootDir.listFiles();
		for (File dateDir : dateDirs) {
			File[] serviceDirs = dateDir.listFiles();
			for (File serviceDir : serviceDirs) {
				File[] methodDirs = serviceDir.listFiles();
				for (File methodDir : methodDirs) {
					String methodUri = CHARTS_DIRECTORY + "/" + dateDir.getName() + "/" + serviceDir.getName() + "/"
							+ methodDir.getName();
                	buildChart(methodUri, methodDir, serviceDir, dateDir, "ms", SUCCESS);
                	buildChart(methodUri, methodDir, serviceDir, dateDir, "count", CONCURRENT);
				}
			}
		}
	}
	
	/**
	 * 构建表格数据并创建表格
	 * @param methodUri
	 * @param methodDir
	 * @param serviceDir
	 * @param dateDir
	 * @param key
	 * @param type
	 */
	private static void buildChart(String methodUri, File methodDir, File serviceDir, File dateDir, String key, String type) {
		File file = new File(methodUri + "/" + type + ".png");
    	long modified = file.lastModified();
    	boolean isChanged = false;
    	Map<String, Long> data = new HashMap<String, Long>();
    	double[] summary = new double[4];
    	
    	File newFile = new File(methodDir, CONSUMER + "." + type);
    	appendData(newFile, data, summary);
    	if (newFile.lastModified() > modified) {
    		isChanged = true;
    	}
		if (isChanged) {
			createChart(key, serviceDir.getName(), methodDir.getName(), dateDir.getName(), CONSUMER, data, summary,
					file.getAbsolutePath());
		}
	}
	
	/**
	 * 计算最大，最小，平均，及请求次数等数据
	 * @param file
	 * @param data
	 * @param summary
	 */
	private static void appendData(File file, Map<String, Long> data, double[] summary) {
        if (!file.exists()) {
            return;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            try {
                int sum = 0;
                int cnt = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    int index = line.indexOf(" ");
                    if (index > 0) {
                        String key = line.substring(0, index).trim();
                        long value = Long.parseLong(line.substring(index + 1).trim());
                        if (!data.containsKey(key)) {
                        	data.put(key, value);
                        } else {
                        	long maxVal = Math.max(data.get(key), value);
                        	data.put(key, maxVal);
                        }
                        summary[0] = Math.max(summary[0], value);
                        summary[1] = summary[1] == 0 ? value : Math.min(summary[1], value);
                        sum += value;
                        cnt ++;
                    }
                }
                summary[3] = cnt;
                summary[2] = sum / cnt;
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
    }
	
	/**
	 * 创建表格视图
	 * @param key
	 * @param service
	 * @param method
	 * @param date
	 * @param type
	 * @param data
	 * @param summary
	 * @param path
	 */
	private static void createChart(String key, String service, String method, String date, String type, Map<String, Long> data, double[] summary, String path) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        DecimalFormat numberFormat = new DecimalFormat("###,##0.##");
        TimeSeriesCollection xydataset = new TimeSeriesCollection();
        TimeSeries timeseries = new TimeSeries(type);
        for (Map.Entry<String, Long> entry : data.entrySet()) {
            try {
                timeseries.add(new Minute(dateFormat.parse(date + entry.getKey())), entry.getValue());
            } catch (ParseException e) {
                logger.error(e.getMessage(), e);
            }
        }
        xydataset.addSeries(timeseries);
        JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(
                "max: " + numberFormat.format(summary[0]) + (summary[1] >=0 ? " min: " + numberFormat.format(summary[1]) : "") 
                + " avg: " + numberFormat.format(summary[2]) + (summary[3] >=0 ? " num: " + numberFormat.format(summary[3]) : ""), 
                toDisplayService(service) + "  " + method + "  " + toDisplayDate(date), key, xydataset, true, true, false);
        jfreechart.setBackgroundPaint(Color.WHITE);
        XYPlot xyplot = (XYPlot) jfreechart.getPlot();
        xyplot.setBackgroundPaint(Color.WHITE);
        xyplot.setDomainGridlinePaint(Color.GRAY);
        xyplot.setRangeGridlinePaint(Color.GRAY);
        xyplot.setDomainGridlinesVisible(true);
        xyplot.setRangeGridlinesVisible(true);
        DateAxis dateaxis = (DateAxis) xyplot.getDomainAxis();
        dateaxis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
        BufferedImage image = jfreechart.createBufferedImage(600, 300);
        try {
            if (logger.isInfoEnabled()) {
                logger.info("write chart: " + path);
            }
            File methodChartFile = new File(path);
            File methodChartDir = methodChartFile.getParentFile();
            if (methodChartDir != null && ! methodChartDir.exists()) {
                methodChartDir.mkdirs();
            }
            FileOutputStream output = new FileOutputStream(methodChartFile);
            try {
                ImageIO.write(image, "png", output);
                output.flush();
            } finally {
                output.close();
            }
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
    }
	
	/**
	 * 
	 * @param service
	 * @return
	 */
	private static String toDisplayService(String service) {
        int i = service.lastIndexOf('.');
        if (i >= 0) {
            return service.substring(i + 1);
        }
        return service;
    }
	
	/**
	 * 
	 * @param date
	 * @return
	 */
	private static String toDisplayDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("yyyyMMdd").parse(date));
        } catch (ParseException e) {
            return date;
        }
    }

}