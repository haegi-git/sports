import styled from "styled-components";
import {useEffect, useState} from "react";
import axios from "axios";

const PostContainer = styled.div`
    width: 1200px;
    height: 800px;
    margin: 0 auto;
    >form{
        width: 100%;
        >input,textarea{
            width: 100%;
            padding: 25px;
        }
        >textarea{
            min-height: 500px;
            max-height: 500px;
            resize: none;
        }
        >select{
            padding: 15px 30px;
        }
    }
    
    

`



function Home() {

    const [data,setData] = useState('')
    useEffect(() => {
        const fetchData = async () => {
            try{
                const response = await axios.get('http://localhost:8090/api/data');
                setData(response.data);
            }catch (error) {
                console.log(error);
            }
        };
        fetchData();
    }, []);

    console.log(data)

    const [images, setImages] = useState([]);

    const handleImageChange = (e) => {
        const files = Array.from(e.target.files); // 선택된 파일 목록
        const imagePreviews = files.map((file) => URL.createObjectURL(file)); // 미리보기 URL 생성
        setImages((prevImages) => prevImages.concat(imagePreviews)); // 기존 이미지와 새 이미지를 합칩니다.
    };

    const handleRemoveImage = (index) => {
        setImages((prevImages) => prevImages.filter((_, i) => i !== index)); // 선택한 이미지 제거
    };

    return (
        <div>
            <h1>메인</h1>
            <h1>스프링부트에서 레스트컨트롤러로 보낸 값 : {data}</h1>

            <PostContainer>
                <form>
                    <select>
                        <option name={"soccer"} value={"soccer"}>축구</option>
                        <option name={"basketball"} value={"basketball"}>농구</option>
                        <option name={"pingpong"} value={"pingpong"}>탁구</option>
                    </select>
                    <input id={"title"} name={"title"}/>

                    <textarea/>
                    <div style={{display: 'flex', overflowX: 'auto'}}>
                        {images.map((image, index) => (
                            <div key={index} style={{position: 'relative', margin: '5px'}}>
                                <img src={image} alt={`preview ${index}`} style={{width: '100px', height: '100px'}}/>
                                <button
                                    onClick={() => handleRemoveImage(index)}
                                    style={{
                                        position: 'absolute',
                                        top: '5px',
                                        right: '5px',
                                        background: 'red',
                                        color: 'white',
                                        border: 'none',
                                        borderRadius: '50%',
                                        cursor: 'pointer',
                                    }}
                                >
                                    X
                                </button>
                            </div>
                        ))}
                    </div>
                    <input type="file" multiple accept="image/*" onChange={handleImageChange}/>
                    <input type={"submit"} id={"content"} name={"content"}/>
                </form>
            </PostContainer>
        </div>
    )
}

export default Home;