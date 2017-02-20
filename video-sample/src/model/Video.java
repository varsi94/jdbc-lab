package model;

import java.time.LocalDate;

public class Video {
	private Integer videoId;
	private String title;
	private String director;
	private LocalDate appearance;
	private Integer duration;
	private Integer fee;
	private String commentLine;
	private String type;

	public Integer getVideoId() {
		return videoId;
	}

	public void setVideoId(Integer videoId) {
		this.videoId = videoId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDirector() {
		return director;
	}

	public void setDirector(String director) {
		this.director = director;
	}

	public LocalDate getAppearance() {
		return appearance;
	}

	public void setAppearance(LocalDate appearance) {
		this.appearance = appearance;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public Integer getFee() {
		return fee;
	}

	public void setFee(Integer fee) {
		this.fee = fee;
	}

	public String getCommentLine() {
		return commentLine;
	}

	public void setCommentLine(String commentLine) {
		this.commentLine = commentLine;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Video [videoId=" + videoId + ", title=" + title + ", director=" + director + "]";
	}

}
