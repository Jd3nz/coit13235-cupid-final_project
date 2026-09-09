# Performance Test Results

## Requirement

`NFR_100Users`: The Cupid Matching component must support 100
simultaneous users.

## Test purpose

The purpose of this test was to verify that the web component could
handle requests from 100 simulated users without errors or unacceptable
response times.

## Test environment

- Apache JMeter 5.6.3
- Java 17 or newer
- Spring Boot
- Spring Data JPA
- MySQL
- Local Windows development environment
- Tested endpoint: `GET /`

## JMeter configuration

- Number of users: 100
- Ramp-up period: 1 second
- Loop count: 10
- Expected requests: 1,000

## Results

- Total requests: 1,000
- Successful requests: 1,000
- Failed requests: 0
- Error rate: 0.00%
- Average response time: 2.22 ms
- Minimum response time: 1 ms
- Maximum response time: 22 ms
- Median response time: 2 ms
- 90th percentile: 3 ms
- 95th percentile: 4 ms
- 99th percentile: 7.99 ms
- Throughput: 1,039.50 requests per second
- APDEX score: 1.000

## Performance targets

The selected performance targets were:

- Support 100 simultaneous users
- Error rate below 1%
- Average response time below 1,000 ms
- Maximum response time below 5,000 ms

All selected targets were achieved.

## Conclusion

The tested homepage endpoint successfully handled 100 simulated users
and 1,000 requests without failures. The average response time was
2.22 ms and the maximum response time was 22 ms.

This test provides evidence that the web component satisfies
`NFR_100Users` for the tested read-only endpoint.

A limitation is that this test measured the homepage endpoint rather
than concurrent swipe database writes. A separate write-load test could
be added to evaluate the full matching and persistence workflow.