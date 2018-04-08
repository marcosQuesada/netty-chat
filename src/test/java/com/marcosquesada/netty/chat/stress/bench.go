package main

import (
	"net"
	"fmt"
	"time"
	"bufio"
	"os"
	"os/signal"
	"syscall"
	"sync"
	"flag"
	"strings"
)

func main() {
	total := flag.Int("total", 10, "total clients")
	flag.Parse()

	done := make(chan struct{})
	for i := 0; i < *total; i++ {

		go func(index int) {
			topic := index % (*total/10)
			client := NewClient(
				fmt.Sprintf("user_%d", index),
				"127.0.0.1:9999",
				fmt.Sprintf("topic_%d", topic),
				done)
			client.run()
		}(i)
	}

	wg := sync.WaitGroup{}
	wg.Add(1)
	c := make(chan os.Signal, 1)
	signal.Notify(c, os.Kill, os.Interrupt, syscall.SIGHUP, syscall.SIGINT, syscall.SIGTERM, syscall.SIGQUIT)
	go func() {
		<-c
		close(done)
		wg.Done()
	}()

	wg.Wait()
}

type Client struct {
	userName, uri, topic string
	conn                 net.Conn
	done                 chan struct{}
	response             chan string
}

func NewClient(u, uri, topic string, d chan struct{}) *Client {
	c := &Client{
		userName: u,
		uri:      uri,
		topic:    topic,
		done:     d,
		response: make(chan string),
	}

	conn, err := net.Dial("tcp", c.uri)
	if err != nil {
		panic(err.Error())
	}

	c.conn = conn

	go c.readLoop()

	return c
}

func (c *Client) run() {
	c.login()

	msg := <-c.receive()
	fmt.Printf("user %s login response: %s \n", c.userName, msg)

	c.join()
	msg = <-c.receive()
	fmt.Printf("user %s join topic %s response: %s", c.userName, c.topic, msg)

	msg = strings.Trim(msg, "\n")
	if !strings.Contains(msg, "Joined") {
		panic("Unexpected joined response " + msg + fmt.Sprintf(" user %s topic %s", c.userName, c.topic))
	}

	ticker := time.NewTicker(time.Millisecond * 200)
	for {
		select {
		case <-ticker.C:
			c.send(fmt.Sprintf("Bla from user %s", c.userName))
		case msg, open := <-c.receive():
			if !open {
				return
			}
			fmt.Printf("User %s Receive %s \n", c.userName, msg)
		case <-c.done:
			return
		}
	}
}

func (c *Client) readLoop() {
	for {
		message, err := bufio.NewReader(c.conn).ReadString('\n')
		if err != nil {
			fmt.Printf("User %s Receive error %s exit", c.userName, err.Error())
			close(c.response)
			return
		}

		c.response <- message
	}

}

func (c *Client) receive() chan string {
	return c.response
}

func (c *Client) login() {
	c.send(fmt.Sprintf("/login %s pass\n", c.userName))
}

func (c *Client) join() {
	c.send(fmt.Sprintf("/join %s", c.topic))
}
func (c *Client) send(msg string) {
	fmt.Fprint(c.conn, fmt.Sprintf("%s\n", msg))
}
