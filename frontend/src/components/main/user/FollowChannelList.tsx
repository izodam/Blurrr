import React, { useRef } from "react";
import Slider from "react-slick";
import styled from "styled-components";
import "slick-carousel/slick/slick.css";
import "slick-carousel/slick/slick-theme.css";
import { MdArrowBackIos } from "react-icons/md";
import { LuDot } from "react-icons/lu";

import FollowChannelCard from "./FollowChannelCard";
import { Channels } from "@/types/channelType";

interface ChannelsProp {
  followChannels: Channels[];
}

const FollowChannelList: React.FC<ChannelsProp> = ({ followChannels }) => {
  console.log(followChannels);
  const settings = {
    dots: true,
    infinite: true,
    centerPadding: "60px",
    slidesToShow: 2,
    slidesToScroll: 1,
    autoplay: true,
    autoplaySpeed: 5000,
    pauseOnHover: true,
  };

  return (
    <Container>
      <CarouselContainer>
        <Slider {...settings}>
          {followChannels.map((channel, index) => (
            <FollowChannelCard
              key={index}
              title={channel.name}
              followers={channel.followCount}
              img={channel.imgUrl}
              id={channel.id}
            />
          ))}
        </Slider>
      </CarouselContainer>
    </Container>
  );
};

export default FollowChannelList;

const Container = styled.div`
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1;
`;

const CarouselContainer = styled.div`
  .slick-list {
    z-index: 10;
  }
  .slick-slider {
    display: flex;
    flex-direction: column;
  }
  .slick-list {
    width: 200px;
    display: flex;
    gap: 10px;
    flex-direction: column;
    justify-content: center;
  }
  .slick-prev,
  .slick-next {
    display: none;
  }

  .slick-prev:before,
  .slick-next:before {
    display: none;
    color: black; /* 화살표 색상 */
    font-size: 20px;
  }

  .slick-next:before {
    content: "";
  }
  .slick-dots {
    position: relative;
    height: 20px;
    bottom: 0px;
    z-index: 0;
  }

  .slick-dots li button:before {
    color: #4b574b;
    font-size: 10px;
  }

  div {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 3px;
  }

  @media (min-width: 900px) {
    .slick-list {
      width: 290px;
    }
  }
`;
