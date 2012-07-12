anovaApproaches <- function(approachAndTime1, approachAndTime2, approachAndTime3, approachAndTime4, plotFileName, removeOutliers=TRUE, exportPath=NULL) {

	if (removeOutliers) {
		size = 8
	} else {
		size = 10
	}

	name = levels(droplevels(approachAndTime1[1,1]))
	a2 = replicate(size, name)

	name = levels(droplevels(approachAndTime2[1,1]))
	a3 = replicate(size, name)

	name = levels(droplevels(approachAndTime3[1,1]))
	a4 = replicate(size, name)
  
	name = levels(droplevels(approachAndTime4[1,1]))
	a5 = replicate(size, name)
  
  #approachesNames = c(a2, a3, a4)
	approachesNames = c(a2, a3, a4, a5)

	time1 = NULL
	time2 = NULL
	time3 = NULL
	time4 = NULL
 	for (i in 2:11) {
 		time1 = c(time1, approachAndTime1[1, i])
 		time2 = c(time2, approachAndTime2[1, i])
 		time3 = c(time3, approachAndTime3[1, i])
 		time4 = c(time4, approachAndTime4[1, i])
 	}

  if (removeOutliers) {
    time1 = removeOutliers(time1)
    time2 = removeOutliers(time2)
    time3 = removeOutliers(time3)
    time4 = removeOutliers(time4)
	}

	approaches = as.factor(approachesNames)
  #A5
	time = c(time1, time2, time3, time4)
	#time = c(time1, time2, time3)
  
	aovResults = aov(time~approaches)
  
	tukeyHSD = TukeyHSD(aovResults)
	
  #Add red colors to p-values greater than 0.05
  library(utils)
  numberOfApproaches = length(approachesNames) / size
  tukeyHSDMatrix = as.matrix(tukeyHSD$approach)
	p = (length(combn(numberOfApproaches, 2))) / 2
	for (i in 1:p) {
	  if (as.numeric(tukeyHSDMatrix[i,4]) > 0.05) {
	    tukeyHSDMatrix[i,4] = paste("<font color=#FF0000>", tukeyHSDMatrix[i,4], "</font>", sep="")
	  } else {
	    tukeyHSDMatrix[i,4] = paste("<font color=#0000FF>", tukeyHSDMatrix[i,4], "</font>", sep="")
	  }
	}
  
	HTML(tukeyHSDMatrix, file=htmlFile, append=TRUE)
  
	png(paste(exportPath, plotFileName, sep=""))
  plot(tukeyHSD, cex.axis=.7)
  dev.off()
  
	HTMLInsertGraph(file=htmlFile, GraphFileName=paste(exportPath, plotFileName, sep=""), Align="center", append=TRUE)
  
	as.matrix(summary(aovResults))
}

#Remove the outliers (max and min)
removeOutliers <- function(data) {
  data = data[data != max(data)]
  data = data[data != min(data)]
  data
}

#Generates the beanplots for all approaches. analysis parameter should be "RD" or "UV".
generateBeanplots <- function(notLazyData, lazyData, analysis, plotFileName, exportPath=NULL, removeOutliers=TRUE) {
  
  #Defines the size of the data.frames depending on the outliers removal
  if (removeOutliers) {
    size = 8
  } else {
    size = 10
  }

  if (analysis == "RD") {
    row = 0
  } else if (analysis == "UV") {
    row = 2
  }

  data = data.frame(rbind(
    notLazyData[(2+row),], notLazyData[(1+row),], lazyData[(2+row),], lazyData[(1+row),]),
        stringsAsFactors=TRUE)

  a2Label = replicate(size, paste(analysis, " A2", sep=""))
  a3Label = replicate(size, paste(analysis, " A3", sep=""))
  a4Label = replicate(size, paste(analysis, " A4", sep=""))
  a5Label = replicate(size, paste(analysis, " A5", sep=""))

  #approaches = c(a2Label, a3Label, a4Label)
  approaches = c(a2Label, a3Label, a4Label, a5Label)
  
  if (removeOutliers) {
    timeA2 = removeOutliers(data[1, c(2:11)])
    timeA3 = removeOutliers(data[2, c(2:11)])
    timeA4 = removeOutliers(data[3, c(2:11)])
    timeA5 = removeOutliers(data[4, c(2:11)])
  } else {
    timeA2 = data[1, c(2:11)]
    timeA3 = data[2, c(2:11)]
    timeA4 = data[3, c(2:11)]
    timeA5 = data[4, c(2:11)]
  }
  
  times = c(timeA2, timeA3, timeA4, timeA5)
  #times = c(timeA2, timeA3, timeA4)
  times = as.vector(times, mode="numeric")
  
  dataForBeanplot = data.frame(approach=approaches, time=times)
  
  attach(dataForBeanplot)
  ap = factor(approach)
  
  png(paste(exportPath, plotFileName, sep=""))
  beanplot(time~ap, col=c("black", "white", "blue", "red"), ylab="Time", xlab="Approach")
  dev.off()
  
  HTMLInsertGraph(file=htmlFile, GraphFileName=paste(exportPath, plotFileName, sep=""), Align="center", append=TRUE)
}

generateBarplot <- function(data, referencePosition, plotFileName, exportPath=NULL) {
  png(paste(exportPath, plotFileName, sep=""))
  bp = barplot(t(data.matrix(data[,-1])), names.arg=data[,1])
  percentages = c(data[1,2] / referencePosition,
                  data[2,2] / referencePosition,
                  data[3,2] / referencePosition,
                  data[4,2] / referencePosition)
  text(cex=1, x=bp, y=data[,2]+par("cxy")[2]/2, paste(round(percentages * 100, digits = 1), "%", sep=""), xpd=TRUE)
  dev.off()
  
  HTMLInsertGraph(file=htmlFile, GraphFileName=paste(exportPath, plotFileName, sep=""), Align="center", append=TRUE)
}

library(gdata)

#root path.
importPath = "/Users/marcinho/Dropbox/Meus Artigos/2013/TAOSD/TAOSD-experiment/maq-tarsis/"

#export path
exportPath = "/Users/marcinho/Dropbox/Meus Artigos/2013/TAOSD/TAOSD-experiment/maq-tarsis/resume/"

#html file
htmlFile = "/Users/marcinho/Dropbox/Meus Artigos/2013/TAOSD/TAOSD-experiment/maq-tarsis/resume/Resume-Results.html"

#creates the export path, if it does not exist. If the html file already exists, delete it.
if (!file.exists(exportPath)) {
  dir.create(file.path(exportPath))
  setwd(file.path(exportPath))
} else {
  if (file.exists(htmlFile)) {
    file.remove(htmlFile)
  }
}

#RD positions according to the *data frame* loaded from the files of each benchmark
a2_RD_position = 2
a3_RD_position = 1
a4_RD_position = 2
a5_RD_position = 1

#UV positions according to the *data frame* loaded from the files of each benchmark
a2_UV_position = 4
a3_UV_position = 3
a4_UV_position = 4
a5_UV_position = 3

#The following arrays are sorted according to the resume.xls file. Do not change this order!
benchmarksNames = c("Berkeley DB", "GPL", "Lampiro", "MobileMedia08")
notLazyFileNames = c("berkeleydb-fs-summary.xls", "graph-product-line-fs-summary.xls", "lampiro-fs-summary.xls", "mobilemedia08-fs-summary.xls")
lazyFileNames = c("berkeleydb-fs-lazy-summary.xls", "graph-product-line-fs-lazy-summary.xls", "lampiro-fs-lazy-summary.xls", "mobilemedia08-fs-lazy-summary.xls")

#Variable used to focus on the columns 1, 4, 7, and 10 of the summary.
#These columns point to the approaches of each benchmark. To reach the data, we do "column + 1"
column = 1

#GPL to test...
#benchmarksNames = c("GPL")
#notLazyFileNames = c("graph-product-line-fs-summary.xls")
#lazyFileNames = c("graph-product-line-fs-lazy-summary.xls")
#column = 4

#Lampiro to test...
#benchmarksNames = c("Lampiro")
#notLazyFileNames = c("lampiro-fs-summary.xls")
#lazyFileNames = c("lampiro-fs-lazy-summary.xls")
#column = 7

resume = read.xls(paste(importPath, "resume.xls", sep=""), sheet=1, header=FALSE)

for (i in 1:length(benchmarksNames)) {
	notLazy = read.xls(paste(importPath, notLazyFileNames[i], sep=""), sheet=11, header=FALSE)
	lazy = read.xls(paste(importPath, lazyFileNames[i], sep=""), sheet=11, header=FALSE)

	a2_RD = notLazy[a2_RD_position,]
	a3_RD = notLazy[a3_RD_position,]
	a4_RD = lazy[a4_RD_position,]
	a5_RD = lazy[a5_RD_position,]

	a2_UV = notLazy[a2_UV_position,]
	a3_UV = notLazy[a3_UV_position,]
	a4_UV = lazy[a4_UV_position,]
	a5_UV = lazy[a5_UV_position,]

  #Narrows tha lazy and not lazy data
  lazyData = lazy[c(1:4), c(1:11)]
  notLazyData = notLazy[c(1:4), c(1:11)]
  
	library(R2HTML)

  #Data
	HTML(paste("<hr><h2>", benchmarksNames[i], " - Data</h2>", sep=""), file=htmlFile, append=TRUE)
	HTML(as.matrix(notLazyData), file=htmlFile, append=TRUE)
	HTML(as.matrix(lazyData), file=htmlFile, append=TRUE)
  
	HTML(paste("<hr><h3>", benchmarksNames[i], " - Barplots</h3>", sep=""), file=htmlFile, append=TRUE)
  
  #Narrows the RD and UV data from the resume file
  rdBarData = resume[c(6:9), c(column:(column+1))]
	uvBarData = resume[c(2:5), c(column:(column+1))]
  
	#Inverting uv bar data since the sheet generated inverted the approaches order: A5, A4, A3, A2
	uvBarData = uvBarData[order(nrow(uvBarData):1),]
  
  #Graphic file names
	rdBarplotFileName = paste("Barplot-", benchmarksNames[i], "-RD.png", sep="")
	uvBarplotFileName = paste("Barplot-", benchmarksNames[i], "-UV.png", sep="")
  rdBeanplotFileName = paste("Beanplot-", benchmarksNames[i], "-RD", ".png", sep="")
	uvBeanplotFileName = paste("Beanplot-", benchmarksNames[i], "-UV", ".png", sep="")
  
  #Position of our "100%" reference. Usually, it is the A2 approach, which is in the [1, 2] position.
	rdReferenceApproachPosition = rdBarData[1, 2]
	uvReferenceApproachPosition = uvBarData[1, 2]
  
  #Barplots
	generateBarplot(rdBarData, rdReferenceApproachPosition, rdBarplotFileName)
	generateBarplot(uvBarData, uvReferenceApproachPosition, uvBarplotFileName)
  
	HTML(paste("<hr><h3>", benchmarksNames[i], " - Beanplots WITHOUT outliers</h3>", sep=""), file=htmlFile, append=TRUE)
  
  #Beanplots
  library(beanplot)
  generateBeanplots(notLazyData, lazyData, "RD", rdBeanplotFileName)
	generateBeanplots(notLazyData, lazyData, "UV", uvBeanplotFileName)
  
  #TukeyHSD graphics file names
	rd10TukeyFileName = paste("TukeyHSD-10-", benchmarksNames[i], "-RD.png", sep="")
	uv10TukeyFileName = paste("TukeyHSD-10-", benchmarksNames[i], "-UV.png", sep="")
	rd8TukeyFileName = paste("TukeyHSD-8-", benchmarksNames[i], "-RD.png", sep="")
	uv8TukeyFileName = paste("TukeyHSD-8-", benchmarksNames[i], "-UV.png", sep="")
  
  #ANOVA
	HTML(paste("<hr><h3>", benchmarksNames[i], " - ANOVA Summary WITH outliers: RD</h3>", sep=""), file=htmlFile, append=TRUE)
  anova = anovaApproaches(a2_RD, a3_RD, a4_RD, a5_RD, rd10TukeyFileName, FALSE)
	HTML(anova, file=htmlFile, append=TRUE)

	HTML(paste("<hr><h3>", benchmarksNames[i], " - ANOVA Summary WITH outliers: UV</h3>", sep=""), file=htmlFile, append=TRUE)
	anova = anovaApproaches(a2_UV, a3_UV, a4_UV, a5_UV, uv10TukeyFileName, FALSE)
	HTML(anova, file=htmlFile, append=TRUE)

	HTML(paste("<hr><h3>", benchmarksNames[i], " - ANOVA Summary WITHOUT outliers: RD</h3>", sep=""), file=htmlFile, append=TRUE)
	anova = anovaApproaches(a2_RD, a3_RD, a4_RD, a5_RD, rd8TukeyFileName)
	HTML(anova, file=htmlFile, append=TRUE)

	HTML(paste("<hr><h3>", benchmarksNames[i], " - ANOVA Summary WITHOUT outliers: UV</h3>", sep=""), file=htmlFile, append=TRUE)
	anova = anovaApproaches(a2_UV, a3_UV, a4_UV, a5_UV, uv8TukeyFileName)
	HTML(anova, file=htmlFile, append=TRUE)
  
  column = column + 3
}