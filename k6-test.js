import http from 'k6/http';
import { sleep, check } from 'k6';

export default function () {
  let r = Math.floor(Math.random() * 1000) + 1;
  const res = http.get(`http://localhost:8080/persons/${r}`);
  check(res, {
    'is status 200': (res) => res.status === 200
  });
  sleep(1);
}