import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import useImageUploader from "../../hooks/useImageUploader";
import { handleChange } from "../../Utils/handleChange";
import { Container } from "../../styled/Container";
import { Form } from "../../styled/Form";
import { useSelector } from "react-redux";
import {postTokenData, putTokenData} from "../../Server/ApiService";
import {fetchData} from "../../Server/ApiServiceNoToken";
import styled from "styled-components";


const FormTest = styled.form`
    width: 50%;
    margin: auto;
    display: flex;
    flex-direction: column;
    >input,select{
        padding:15px;
        margin:15px 0;
    }
`


function ShopAdd() {
    const { id } = useParams(); // 상품 ID를 URL에서 가져옴
    const navigate = useNavigate();

    const [addItem, setAddItem] = useState({
        title: "",
        price: "",
        desc: "",
        file: "",
        stock: "",
        categoryId: ""
    });

    const { images, handleImageChange, handleRemoveImage } = useImageUploader(true);

    const [category,setCategory] = useState()

    useEffect(() => {
        if (id) {
            // 상품 ID가 있는 경우 해당 상품의 상세 정보를 불러옴
            fetchData(`/shop/detail/${id}`)
                .then((res) => {
                    const updateItem = res.data.item
                    setAddItem({
                        title: updateItem.title,
                        price: updateItem.price,
                        desc: updateItem.desc,
                        file: "",
                        stock: updateItem.stock,
                        categoryId: updateItem.categoryId
                    })
                })
        }

        fetchData('/category/get').then((res)=>{
            const filteredCategories = res.data.filter(item=>
            item.tag === 'shop')
            setCategory(filteredCategories);
            setAddItem({...addItem,categoryId: filteredCategories[0].id})
        })
    }, [id]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        const addItemFormData = new FormData();
        addItemFormData.append("title", addItem.title);
        addItemFormData.append("price", addItem.price);
        addItemFormData.append("desc", addItem.desc);
        addItemFormData.append("stock", addItem.stock);
        addItemFormData.append("categoryId", addItem.categoryId);

        if (images.length > 0) {
            images.forEach((image) => {
                addItemFormData.append("file", image.file);
            });
        }

        try {
            if (id) {
                // 수정 요청 (PUT)
                await putTokenData(`/shop/update/${id}`, addItemFormData);
            } else {
                // 추가 요청 (POST)
                await postTokenData("/shop/add", addItemFormData);
            }
            navigate("/shop");
        } catch (error) {
            console.error(error);
        }
    };
console.log(addItem)
    return (
        <Container>
            <h1>{id ? "상품 수정 페이지" : "상품 추가 페이지"}</h1>
            <FormTest onSubmit={handleSubmit} encType="multipart/form-data">
                <input
                    onChange={(e) => handleChange(e, addItem, setAddItem)}
                    type="text"
                    placeholder="상품명"
                    name="title"
                    value={addItem.title}
                />
                <input
                    onChange={(e) => handleChange(e, addItem, setAddItem)}
                    type="text"
                    placeholder="가격"
                    name="price"
                    value={addItem.price}
                />
                <input
                    onChange={(e) => handleChange(e, addItem, setAddItem)}
                    type="text"
                    placeholder="상품소개"
                    name="desc"
                    value={addItem.desc}
                />
                <input type="file" name="file" multiple onChange={handleImageChange} />
                <input
                    onChange={(e) => handleChange(e, addItem, setAddItem)}
                    type="text"
                    placeholder="재고"
                    name="stock"
                    value={addItem.stock}
                />
                <select
                    name="categoryId"
                    onChange={(e) => handleChange(e, addItem, setAddItem)}
                    value={addItem.categoryId}
                >
                    {category?.map((item)=> {
                        return(
                        <option key={item.id} value={item.id}>{item.name}</option>
                        )
                    })}
                </select>

                <div style={{ display: 'flex', overflowX: 'auto' }}>
                    {images.map((image, index) => (
                        <div key={index} style={{ position: 'relative', margin: '5px' }}>
                            <img src={image.preview} alt={`preview ${index}`}
                                 style={{ width: '100px', height: '100px' }} />
                            <button
                                type="button"
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

                <input type="submit" value={id ? "상품 수정" : "상품 등록"} />
            </FormTest>
        </Container>
    );
}

export default ShopAdd;
