import { Spinner } from "react-bootstrap";

const LoadSpinner = () => {
  return (
    <div className="d-flex justify-content-center align-items-center" style={{ minHeight: "300px" }}>
      <Spinner animation="border" role="status" variant="warning">
        <span className="visually-hidden">Loading...</span>
      </Spinner>
    </div>
  );
};

export default LoadSpinner;
