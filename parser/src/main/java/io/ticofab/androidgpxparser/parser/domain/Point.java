package io.ticofab.androidgpxparser.parser.domain;

//import org.joda.time.DateTime;

import java.time.LocalDateTime;

/**
 * A point containing a location, time and name.
 */
public abstract class Point {
    private final Double mLatitude;
    private final Double mLongitude;
    private final Double mElevation;
    private final LocalDateTime mTime;
    private final String mName;
    private final String mDesc;
    private final String mType;

    Point(Builder builder) {
        mLatitude = builder.mLatitude;
        mLongitude = builder.mLongitude;
        mElevation = builder.mElevation;
        mTime = builder.mTime;
        mName = builder.mName;
        mDesc = builder.mDesc;
        mType = builder.mType;
    }

    /**
     * @return the latitude in degrees
     */
    public Double getLatitude() {
        return mLatitude;
    }

    /**
     * @return the longitude in degrees
     */
    public Double getLongitude() {
        return mLongitude;
    }

    /**
     * @return the elevation in meters
     */
    public Double getElevation() {
        return mElevation;
    }

    public LocalDateTime getTime() {
        return mTime;
    }

    /**
     * @return the point name
     */
    public String getName() {
        return mName;
    }

    /**
     * @return the description
     */
    public String getDesc() {
        return mDesc;
    }

    /**
     * @return the type (category)
     */
    public String getType() {
        return mType;
    }

    public static abstract class Builder {
        private Double mLatitude;
        private Double mLongitude;
        private Double mElevation;
        private LocalDateTime mTime;
        private String mName;
        private String mDesc;
        private String mType;

        public Builder setLatitude(Double latitude) {
            mLatitude = latitude;
            return this;
        }

        public Builder setLongitude(Double longitude) {
            mLongitude = longitude;
            return this;
        }

        public Builder setElevation(Double elevation) {
            mElevation = elevation;
            return this;
        }

        public Builder setTime(LocalDateTime time) {
            mTime = time;
            return this;
        }

        public Builder setName(String mame) {
            mName = mame;
            return this;
        }

        public Builder setDesc(String desc) {
            mDesc = desc;
            return this;
        }

        public Builder setType(String type) {
            mType = type;
            return this;
        }

        public abstract Point build();
    }
}
